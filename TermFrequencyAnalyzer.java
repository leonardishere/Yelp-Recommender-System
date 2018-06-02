import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * Takes the reviews from the database and sends the review text to the POS Tagger.
 * Waits for the POS Tagger to return a list of terms that are nouns and adjectives.
 * Then counts how many reviews each one of those words is in.
 * After it's done counting how many reviews it's in, divides each by total number of reviews,
 * ranks from highest to lowest, and takes top k terms. Top k terms get combined with other attributes
 * and sent to Andrew's algorithm.
 *
 * Work in progress, still testing this.
 */

public class TermFrequencyAnalyzer {

    //Maps the word (either noun or adjective) to the number of reviews it appears in.
    Map<String, Integer> termToReviewCount = new HashMap<>();

    List<String> businessIDs = new LinkedList<>();

    List<String> reviews = new ArrayList<>();

    List<String> keyTerms = new ArrayList<>();

    private static final String NOUN = "N"; // Starts with N, handles NN, NNS, NNP, NNPS
    private static final String ADJECTIVE = "J"; // Starts with J, handles JJ, JJR, JJS
    MaxentTagger tagger = null;

    //Number of nouns/adjectives to extract from the reviews for each business
    Integer k = 10;

    Integer currentPrimaryKey = 0;


    //BusinessID, Term, TermRank
    public void initializeDB() {
        System.out.println("Calling Initialize");
        //Drop the K term table then Create it again for a fresh start.
        List<String> statements = new LinkedList<>();
        statements.add("DROP TABLE 'BusinessKeyTerms';");
        statements.add("CREATE TABLE BusinessKeyTerms(id varchar(255)," +
                "businessID varchar(255), keyTerm varchar(255));");
        writeToTable(statements, "BUSINESSKEYTERMS");

    }

    //This method used from open source code located here: http://www.sqlitetutorial.net/sqlite-java/select/
    private Connection connect() {
        // SQLite connection string
        String url = "jdbc:sqlite:yelp_db.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
            System.out.println("Connection established successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    //Portions of this code used  from here: http://www.sqlitetutorial.net/sqlite-java/select/
    private void selectFromDB(String selectStatement, String table) {

        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectStatement)) {

            // loop through the result set
            if (table.equals("BUSINESS")) {
                while (rs.next()) {
                    System.out.println("Reading from business table");
                    businessIDs.add(rs.getString("id"));
                }
            } else if (table.equals("REVIEWS")) {
                while (rs.next()) {
                    System.out.println("Reading from review table");
                    String temp = rs.getString("text");
                    reviews.add(temp);
                }
            } else if (table.equals("BUSINESSKEYTERMS")) {
                while (rs.next()) {
                    System.out.println("Reading from businessKeyTerm table");
                    String temp = rs.getString("keyTerm");
                    keyTerms.add(temp);

                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //Counts the number of reviews that each noun/adjective
    //occurs in the original list of business reviews.
    private void countTerms(List<String> nounsAndAdjectives, List<String> reviews) {
        List<String> nounsAndAdjectivesToLower = nounsAndAdjectives.stream().map(String::toLowerCase)
                .collect(Collectors.toList());

        Set<String> terms = new HashSet<>(nounsAndAdjectivesToLower);
        Integer count = 0;
        for (String term : terms) {
            for (String review : reviews) {
                if (review.toLowerCase().contains(term.toLowerCase())) {
                    count++;
                    break;
                }
            }
            termToReviewCount.put(term, count);
        }
    }

    private List<String> getTopKTerms(Map<String, Integer> termToReviewCount) {

        //Sort the terms by values
        List<Integer> values = new ArrayList<>(termToReviewCount.values());
        values.sort(Comparator.naturalOrder());

        //Take the top k values
        List<Integer> topValues = values.subList(values.size() - k, values.size());

        //Get the terms which have a min of the top k values
        List<String> topTerms = termToReviewCount.keySet().stream()
                .filter(x -> termToReviewCount.get(x) > topValues.get(0)).collect(Collectors.toList());

        //Cut the terms at k in case there were any with duplicate values.
        if (topTerms.size() > k) {
            topTerms.subList(0, k);
        }
        System.out.println("Size of top k terms: " + topTerms.size());
        return topTerms;
    }

    //Combine all the reviews into one giant text so only hitting the POSTagger
    //once to improve efficiency (hopefully).
    public String combineReviews(List<String> reviews) {
        StringBuilder sb = new StringBuilder();
        for (String review : reviews) {
            sb.append(review);
            sb.append("\n");
        }
        return sb.toString();
    }

    //Selects all the reivews from the Review table with Rating > 4 && matching businessID
    public void analyzeReviewTextForBusiness(String businessID) {
        //Get all the Reviews with ratings >= 4 Stars
        selectFromDB("SELECT text " +
                        "FROM review WHERE business_id = '" + businessID + "' AND stars >= 4",
                "REVIEWS");

        //Combine text so sent to POSTagger as one large document
        String allReviews = combineReviews(this.reviews);

        //Send them to the POS Tagger
        Map<String, Integer> keyTerms = tagPOS(allReviews);

        countTerms(new ArrayList<>(keyTerms.keySet()), this.reviews);

        //return top k terms
        List<String> terms = getTopKTerms(this.termToReviewCount);
        writeTopKAttributesToTable(businessID, terms);
    }

    private void readInBusinesses() {
        selectFromDB("SELECT id FROM business;", "BUSINESS");
    }

    //Called to get the Top Key Terms for a given business ID from the database.
    public double getKeyTermsFromDB(String businessID1, String businessID2) {
        selectFromDB("Select keyTerm from BusinessKeyTerms Where businessID='" + businessID1 + "';",
                "BUSINESSKEYTERMS");

        List<String> b1KeyTerms = new LinkedList<>(keyTerms);
        keyTerms.clear();

        selectFromDB("Select keyTerm from BusinessKeyTerms Where businessID='" + businessID2 + "';",
                "BUSINESSKEYTERMS");

        List<String> b2KeyTerms = new LinkedList<>(keyTerms);

        //This will make a set of unique terms between the two
        Set<String> b1Set = new HashSet<>(b1KeyTerms);
        Set<String> b2Set = new HashSet<>(b2KeyTerms);

        //B1 now contains the intersect of B1 and B2
        b1Set.retainAll(b2Set);

        double jacardSimlarity = 0;
        if (b1KeyTerms.size() + b2KeyTerms.size() - b1Set.size() != 0) {
            double b1Size = (double) b1Set.size();
            double b1KeyTermsSize = (double) b1KeyTerms.size();
            double b2KeyTermsSize = (double) b2KeyTerms.size();
            jacardSimlarity = b1Size / (b1KeyTermsSize + b2KeyTermsSize - b1Size);
        }
        return jacardSimlarity;

    }

    private void writeTopKAttributesToTable(String businessID, List<String> minedAttributes) {

        List<String> insertStatements = new LinkedList<>();

        for (String keyTerm : minedAttributes) {
            String sqlStatement = "INSERT INTO BusinessKeyTerms(id, businessID, keyTerm) " +
                    "VALUES(" + currentPrimaryKey + ", '" + businessID + "', '" + keyTerm + "';";
            insertStatements.add(sqlStatement);

            currentPrimaryKey++;
        }
        writeToTable(insertStatements, "BUSINESSKEYTERMS");

    }

    //Portions of this code used  from here: http://www.sqlitetutorial.net/sqlite-java/select/
    private void writeToTable(List<String> statements, String table) {
        try (Connection conn = this.connect();) {

            // loop through the result set
            if (table.equals("BUSINESSKEYTERMS")) {
                for (String insertStatement : statements) {
                    Statement stmt = conn.createStatement();
                    stmt.executeUpdate(insertStatement);
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    //Call this ONCE
    public void runTermFrequencyAnalysis() {
        System.out.println("Calling Initialize from Main.");
        initializeDB();
        System.out.println("About to try and read in businesses.");

        readInBusinesses();

        for (String business : businessIDs) {
            System.out.println("Analyzing terms for business with id: " + business);
            analyzeReviewTextForBusiness(business);
        }

    }

    public void initializePOSTagger() {
        //I had to go with this type of structure because my path is different and I didn't want to mess up other peoples builds by just switching it
        try {
            tagger =  new MaxentTagger("lib/english-left3words-distsim.tagger");
        } catch(Exception ex) {
            tagger = null;
        }
        if(tagger == null) {
            tagger = new MaxentTagger("yelp_recommender_system/lib/english-left3words-distsim.tagger");
        }
    }

    public Map<String, Integer> tagPOS(String sentence){
        Map<String, Integer> map = new HashMap<>();

        String taggedString = tagger.tagString(sentence);

        // Separate into <word>_<tag>
        String[] splitTag = taggedString.split(" ");

        for (String splitWord : splitTag){
            // Split <word>_<tag>
            int lastInst = splitWord.lastIndexOf("_");
            String word = splitWord.substring(0, lastInst);
            String tag = splitWord.substring(lastInst + 1, splitWord.length());

            // Check if noun or adjective
            if (tag.startsWith(NOUN) || tag.startsWith(ADJECTIVE)){
                int count = map.containsKey(word) ? map.get(word) : 0;
                map.put(word, count + 1);
            }
        }

        return map;
    }

    public static void main(String args[]) {
        TermFrequencyAnalyzer termFrequencyAnalyzer = new TermFrequencyAnalyzer();
        termFrequencyAnalyzer.runTermFrequencyAnalysis();
    }

}



/*
    public double testSimilarityOfKeyTerms(List<String> keyTerms1, List<String> keyTerms2){

        keyTerms = keyTerms1;

        List<String> b1KeyTerms = new LinkedList<>(keyTerms);
        keyTerms.clear();

        keyTerms = keyTerms2;
        List<String> b2KeyTerms = new LinkedList<>(keyTerms);


        //This will make a set of unique terms between the two
        Set<String> b1Set = new HashSet<>(b1KeyTerms);
        Set<String> b2Set = new HashSet<>(b2KeyTerms);

        //B1 now contains the intersect of B1 and B2
        b1Set.retainAll(b2Set);
*/


//        double jacardSimlarity = 0;
//        if (b1KeyTerms.size() + b2KeyTerms.size() - b1Set.size() !=0) {
//            double b1Size = (double)b1Set.size();
//            double b1KeyTermsSize = (double)b1KeyTerms.size();
//            double b2KeyTermsSize = (double)b2KeyTerms.size();
//            jacardSimlarity = b1Size / (b1KeyTermsSize + b2KeyTermsSize  - b1Size);
//        }
//        return jacardSimlarity;
//    }

//Unit Tests for everything but the SQL
/*    public static void main (String[] args) {
        TermFrequencyAnalyzer analyzer = new TermFrequencyAnalyzer();*/

/*        List<String> testReviews = new ArrayList<>();
        testReviews.add("This is awesome. I love this place. Blah balah blah blah.");
        testReviews.add("I hate this place. The worst.");
        testReviews.add("Best food ever.");

        String testCombineMethod = analyzer.combineReviews(testReviews);
        System.out.println(testCombineMethod);

        Map<String, Integer> keyTermsTest = new HashMap<String, Integer>();
        keyTermsTest.put("Bob", 5);
        keyTermsTest.put("bob", 10);
        keyTermsTest.put("Larry", 13);
        keyTermsTest.put("Larr1y", 11);
        keyTermsTest.put("Larererry", 12);
        keyTermsTest.put("food", 11);
        keyTermsTest.put("Laveewrry", 10);
        keyTermsTest.put("Food", 1);
        keyTermsTest.put("food2", 1);
        keyTermsTest.put("food1", 1);
        keyTermsTest.put("food2", 14);
        keyTermsTest.put("food3", 15);

        analyzer.countTerms(new ArrayList<>(keyTermsTest.keySet()), testReviews);

        List<String> topK = analyzer.getTopKTerms(analyzer.termToReviewCount);*/

/*

        List<String> keyTerms1 = new LinkedList<>();
        keyTerms1.add("test");
        keyTerms1.add("food");
        keyTerms1.add("seven");
        keyTerms1.add("good");


        List<String> keyTerms2 = new LinkedList<>();
        keyTerms2.add("test");
        keyTerms2.add("food");
        
        keyTerms2.add("seven");
        keyTerms2.add("good");
        keyTerms2.add("meatballs");
        keyTerms2.add("testthis");

        analyzer.testSimilarityOfKeyTerms(keyTerms1, keyTerms2);

    }*/

//}
