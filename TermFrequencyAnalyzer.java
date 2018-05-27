
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

    public enum TableName{BUSINESS, REVIEWS, USERS, BUSSINESSKEYTERMS};

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
    public void selectFromDB(String selectStatement, TableName table){

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
    //Then divides by total number of reviews > 4/5 stars.
    public void countTerms(List<String> nounsAndAdjectives) {
        Integer count = 0;
        for (String term : nounsAndAdjectives) {
            for (String review : reviews) {
                if (review.toLowerCase().contains(term.toLowerCase())){
                    count ++;
                    break;
                }
            }
            termToReviewCount.put(term, count);
        }
    }

    public List<String> getTopKTerms() {

        //Sort the terms by values
        List<Integer> values = (LinkedList<Integer>)termToReviewCount.values();
        values.sort(Comparator.naturalOrder());

        //Take the top k values
        List<Integer> topValues = values.subList(values.size()-k + 1, values.size());

        //Get the terms which have a min of the top k values
        List<String> topTerms = termToReviewCount.keySet().stream()
                .filter(x -> termToReviewCount.get(x) > topValues.get(0)).collect(Collectors.toList());

        //Cut the terms at k in case there were any with duplicate values.
        topTerms.subList(0,k);
        return topTerms;
    }

    //Combine all the reviews into one giant text so only hitting the POSTagger
    //once to improve efficiency (hopefully).
    public String combineReviews(){
        StringBuilder sb = new StringBuilder();
        for (String review : reviews){
            sb.append(review + "/n");
        }
        return sb.toString();
    }

    //Selects all the reivews from the Review table with Rating > 4 && matching businessID
    public List<String> analyzeReviewTextForBusiness(String businessID) {
        //Get all the Reviews with ratings >= 4 Stars
        selectFromDB("SELECT text " +
                "FROM review WHERE business_id = " + businessID + " AND stars >= 4",
                TableName.REVIEWS);

        //Combine text so sent to POSTagger as one large document
        String allReviews = combineReviews();

        //Send them to the POS Tagger
        POSTagger posTagger = new POSTagger();
        Map<String, Integer> keyTerms = posTagger.tagPOS(allReviews);

        countTerms((List)keyTerms.keySet());

        //return top k terms
        return getTopKTerms();
    }

    public void writeTopKAttributesToTable(String businessID, List<String> minedAttributes) {

        List<String> insertStatements = new LinkedList<>();

        for (String keyTerm : minedAttributes) {
            String sqlStatement = "INSERT INTO BusinessToKeyterms(id, businessID, keyTerm) " +
                    "VALUES(" + currentPrimaryKey +", " + businessID + ", " + keyTerm +";";
            insertStatements.add(sqlStatement);

            currentPrimaryKey++;
        }
            writeToTable(insertStatements, TableName.BUSSINESSKEYTERMS);

    }

    private void writeToTable(List<String> statements, TableName table){

    }


}
