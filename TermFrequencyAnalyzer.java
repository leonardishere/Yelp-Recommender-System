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

    Map<String, List<String> > businessToTerms = new HashMap<>();

    Connection connection;

    private static final String NOUN = "N"; // Starts with N, handles NN, NNS, NNP, NNPS
    private static final String ADJECTIVE = "J"; // Starts with J, handles JJ, JJR, JJS
    MaxentTagger tagger = null;

    //Number of nouns/adjectives to extract from the reviews for each business
    Integer k = 10;

    Integer currentPrimaryKey = 0;


    //BusinessID, Term, TermRank
    public void initializeDB() {
        connection = connect();
    }

    //This method used from open source code located here: http://www.sqlitetutorial.net/sqlite-java/select/
    private Connection connect() {
        // SQLite connection string
        String url = "jdbc:sqlite:yelp_backup.db";
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

        System.out.println("Executing this statement: " + selectStatement);

        try (//Connection conn = this.connect();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(selectStatement)) {

            // loop through the result set
            if (table.equals("BUSINESS")) {
                System.out.println("Reading in businesses.");
                while (rs.next()) {
                    businessIDs.add(rs.getString("id"));
                }
                System.out.print("Found Businesses count ");
                System.out.println(businessIDs.size());
            } else if (table.equals("REVIEWS")) {
                reviews.clear();
                System.out.println("Reading in Reviews.");
                while (rs.next()) {
                    String temp = rs.getString("text");
                    reviews.add(temp);
                }
                System.out.print("Found Reviews count ");
                System.out.println(reviews.size());
            } else if (table.equals("BUSINESSKEYTERMS")) {
                keyTerms.clear();
                while (rs.next()) {
                    String temp = rs.getString("keyTerm");
                    keyTerms.add(temp);
                }
                System.out.print("Found Keyterms count: ");
                System.out.println(keyTerms.size());
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
        this.termToReviewCount.clear();

        //Get all the Reviews with ratings >= 4 Stars
        selectFromDB("SELECT text " +
                        "FROM review WHERE business_id = '" + businessID + "' AND stars >= 4 AND postal_code LIKE '_____';",
                "REVIEWS");

        if (reviews.size() > 20) {
            //Combine text so sent to POSTagger as one large document
            String allReviews = combineReviews(this.reviews);

            //Send them to the POS Tagger
            LinkedList<String> keyTerms = tagPOS(allReviews);

            countTerms(keyTerms, this.reviews);

            //return top k terms
            List<String> terms = getTopKTerms(this.termToReviewCount);
            //writeTopKAttributesToTable(businessID, terms);
            businessToTerms.put(businessID, terms);
        } else {
            System.out.println("Not looking at business " + businessID + "because review count is " + reviews.size());
        }
    }

    private void readInBusinesses() {
        selectFromDB("SELECT id FROM business WHERE review_count >=50;", "BUSINESS");
    }

    //Called to get the Top Key Terms for a given business ID from the database.
    //Call initializeDB() function once before.
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

    private void writeTopKAttributesToTable() {

        //Connection connection = connect();
        PreparedStatement preparedStatement = null;
        String sqlStatement = "INSERT INTO BusinessKeyTerms(id, businessID, keyTerm) " +
                "VALUES(?,?,?);";
        try {

            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(sqlStatement);

            for (String businessID : businessToTerms.keySet()) {
                for (String keyTerm : businessToTerms.get(businessID)) {
                    preparedStatement.setInt(1, currentPrimaryKey);
                    preparedStatement.setString(2, businessID);
                    preparedStatement.setString(3, keyTerm);
                    preparedStatement.addBatch();
                    currentPrimaryKey++;
                }
            }
            preparedStatement.executeBatch();
            businessToTerms.clear();
            connection.commit();
            //connection.close();
        } catch (Exception e) {
            System.out.println("Something went wrong with prepared statement");
            System.out.println(e.getMessage());
        }

    }

    //Call this ONCE
    public void runTermFrequencyAnalysis() {
        System.out.println("Initializing POSTagger");
        initializePOSTagger();
        System.out.println("Calling Initialize from Main.");
        initializeDB();
        System.out.println("About to try and read in businesses.");

        readInBusinesses();

        int count = 0;
        int multiple = 0;
        for (String business : businessIDs) {
            System.out.println("Analyzing terms for business with id: " + business);
            analyzeReviewTextForBusiness(business);
            count ++;
            if (count % 1000 == 0) {
                multiple++;
                writeTopKAttributesToTable();
                count = 0;
            }
        }

    }

    public void initializePOSTagger() {
        //I had to go with this type of structure because my path is different and I didn't want to mess up other peoples builds by just switching it
        try {
            tagger =  new MaxentTagger("/local/weka/lib/stanford-postagger-2018-02-27/models/english-left3words-distsim.tagger");
        } catch(Exception ex) {
            tagger = null;
        }
        if(tagger == null) {
            tagger = new MaxentTagger("/local/weka/lib/stanford-postagger-2018-02-27/models/english-left3words-distsim.tagger");
        }

        System.out.println("Tagger intialized. ");
    }

    public LinkedList<String> tagPOS(String sentence){
        LinkedList<String> map = new LinkedList<>();

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
                if (!map.contains(word.toLowerCase())) {
                    map.add(word.toLowerCase());
                }
            }
        }

        return map;
    }

    public static void main(String args[]) {
        TermFrequencyAnalyzer termFrequencyAnalyzer = new TermFrequencyAnalyzer();
        termFrequencyAnalyzer.runTermFrequencyAnalysis();
    }

}

