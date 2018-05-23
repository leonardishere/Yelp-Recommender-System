package yelp_recommender_system;

/**
 * RecommenderSystem is the main class of the yelp recommender system.
 * @author Andrew Leonard
 *
 */
public class RecommenderSystem {
	
	private double[][][] actualRatingMatrix = null;
	private KeyMap userMap = null, businessMap = null;
	private int numStars = 5, numUsers = -1, numBusinesses = -1;
	
	public RecommenderSystem() {
		System.out.println("Begin loading users");
		userMap = DatabaseReader.loadUsers();
		numUsers = userMap.size();
		System.out.println("Begin loading businesses");
		businessMap = DatabaseReader.loadBusinesses();
		numBusinesses = businessMap.size();
		System.out.println("Begin loading reviews");
		actualRatingMatrix = DatabaseReader.loadActualRatingMatrix(userMap, businessMap);
	}
	
	/**
	 * Main method.
	 * @param args ignored
	 */
	public static void main(String[] args) {
		System.out.println("Program start");
		RecommenderSystem recSys = new RecommenderSystem();
		System.out.println("Program end");
	}
}
