//package yelp_recommender_system;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

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

    public enum TableName{BUSINESS, REVIEWS, USERS, BUSINESSKEYTERMS};

    //Maps the word (either noun or adjective) to the number of reviews it appears in.
    Map<String, Integer> termToReviewCount = new HashMap<>();

    List<String> businessIDs = new LinkedList<>();

    List<String> reviews = new ArrayList<>();

    //Number of nouns/adjectives to extract from the reviews for each business
    Integer k = 10;

    Integer currentPrimaryKey = 0;


    //BusinessID, Term, TermRank
    public void initializeDB() {
        //Drop the K term table
        List<String> statements = new LinkedList<>();
        statements.add("DROP TABLE 'BusinessKeyTerms';");
        statements.add("CREATE TABLE BusinessKeyTerms(id varchar(255)," +
                "businessID varchar(255), keyTerm varchar(255));");
        writeToTable(statements, TableName.BUSINESSKEYTERMS);

        //Create the K term table
    }

    //This method used from open source code located here: http://www.sqlitetutorial.net/sqlite-java/select/
    private Connection connect() {
        // SQLite connection string
        String url = "jdbc:sqlite:C://sqlite/db/test.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    //Portions of this code used  from here: http://www.sqlitetutorial.net/sqlite-java/select/
    private void selectFromDB(String selectStatement, TableName table){

        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(selectStatement)){

            // loop through the result set
            if (table.equals(TableName.BUSINESS)) {
                while (rs.next()) {
                    System.out.println("Reading from business table");
                    businessIDs.add(rs.getString("id"));
                }
            } else if (table.equals(TableName.REVIEWS)) {
                while (rs.next()) {
                    System.out.println("Reading from review table");
                    String temp = rs.getString("text");
                    reviews.add(temp);
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
                if (review.toLowerCase().contains(term.toLowerCase())){
                    count ++;
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
        List<Integer> topValues = values.subList(values.size()-k, values.size());

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
    public String combineReviews(List<String> reviews){
        StringBuilder sb = new StringBuilder();
        for (String review : reviews){
            sb.append(review);
            sb.append("\n");
        }
        return sb.toString();
    }

    //Selects all the reivews from the Review table with Rating > 4 && matching businessID
    public void analyzeReviewTextForBusiness(String businessID) {
        //Get all the Reviews with ratings >= 4 Stars
        selectFromDB("SELECT text " +
                "FROM review WHERE business_id = " + businessID + " AND stars >= 4",
                TableName.REVIEWS);

        //Combine text so sent to POSTagger as one large document
        String allReviews = combineReviews(this.reviews);

        //Send them to the POS Tagger
        POSTagger posTagger = new POSTagger();
        Map<String, Integer> keyTerms = posTagger.tagPOS(allReviews);

        countTerms(new ArrayList<>(keyTerms.keySet()), this.reviews);

        //return top k terms
        List<String> terms = getTopKTerms(this.termToReviewCount);
        writeTopKAttributesToTable(businessID, terms);
    }

    private void readInBusinesses() {
        selectFromDB("SELECT id FROM business;", TableName.BUSINESS);
    }

    private void writeTopKAttributesToTable(String businessID, List<String> minedAttributes) {

        List<String> insertStatements = new LinkedList<>();

        for (String keyTerm : minedAttributes) {
            String sqlStatement = "INSERT INTO BusinessKeyTerms(id, businessID, keyTerm) " +
                    "VALUES(" + currentPrimaryKey +", " + businessID + ", " + keyTerm +";";
            insertStatements.add(sqlStatement);

            currentPrimaryKey++;
        }
            writeToTable(insertStatements, TableName.BUSINESSKEYTERMS);

    }

    //Portions of this code used  from here: http://www.sqlitetutorial.net/sqlite-java/select/
    private void writeToTable(List<String> statements, TableName table){
        try (Connection conn = this.connect();){

            // loop through the result set
            if (table.equals(TableName.BUSINESSKEYTERMS)) {
                for(String insertStatement : statements) {
                    Statement stmt  = conn.createStatement();
                    stmt.executeUpdate(insertStatement);
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void runTermFrequencyAnalysis(){
        initializeDB();

        readInBusinesses();

        for (String business : businessIDs) {
            System.out.println("Analyzing terms for business with id: " + business);
            analyzeReviewTextForBusiness(business);
        }

    }


//Unit Tests for everything but the SQL
/*    public static void main (String[] args) {
        TermFrequencyAnalyzer analyzer = new TermFrequencyAnalyzer();

        List<String> testReviews = new ArrayList<>();
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

        List<String> topK = analyzer.getTopKTerms(analyzer.termToReviewCount);

    }*/

}
