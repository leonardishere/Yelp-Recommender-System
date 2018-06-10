import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 * RecommenderSystem is the main class of the yelp recommender system.
 * @author Andrew Leonard
 *
 */
public class RecommenderSystem {

	//public double[][][] actualRatingMatrix = null;
	public User[] users = null;
	public Business[] businesses = null;
	public KeyMap userMap = null, businessMap = null;
	//public int numStars = 5, numUsers = -1, numBusinesses = -1;
	public static final int NUM_STARS = 5, NUM_FEATURES = 41;
	public int numUsers = -1, numBusinesses = -1;
	//public RatingNeuralNetwork net = null;
	public Model model = null;
	public int numIters = 0;
	public ArrayList<UserBusinessInteraction> testingData = null;
	public static final String SQLITE_YELP_DB_URL = "jdbc:sqlite:/local/weka/yelp.db";
	public static final boolean USE_SQLITE = true; //as opposed to mysql
	public static final boolean RESTAURANTS_ONLY = true; //as opposed to all businesses
	public Connection conn = null;

	public RecommenderSystem() {
		conn = USE_SQLITE ? DatabaseReader.connect_sqlite(RESTAURANTS_ONLY) : DatabaseReader.connect_mysql();
		loadUsers();
		loadBusinesses();
		loadAttributes();
		loadCategories();
		loadTrainingRatings();
		loadTestingRatings();
		boolean loadedModel = loadModel();
		if(!loadedModel) createModel();
	}

	public void loadUsers() {
		System.out.println("Begin loading users");
		users = DatabaseReader.loadUsers(conn, RESTAURANTS_ONLY);
		userMap = new KeyMap();
		numUsers = users.length;
		for(int i = 0; i < numUsers; ++i) {
			userMap.add(users[i].id);
		}
	}

	public void loadBusinesses() {
		System.out.println("Begin loading businesses");
		businesses = DatabaseReader.loadBusinesses(conn, RESTAURANTS_ONLY);
		businessMap = new KeyMap();
		numBusinesses = businesses.length;
		for(int i = 0; i < numBusinesses; ++i) {
			businessMap.add(businesses[i].id);
		}
	}

	public void loadAttributes() {
		System.out.println("Begin loading attributes");
		ArrayList<BusinessAttribute> attributes = DatabaseReader.loadAttributes(conn, RESTAURANTS_ONLY);
		for(int i = 0; i < attributes.size(); ++i) {
			BusinessAttribute attribute = attributes.get(i);
			businesses[businessMap.convert(attribute.business_id)].addAttribute(attribute.name, attribute.value);
		}
	}

	public void loadCategories() {
		System.out.println("Begin loading categories");
		ArrayList<BusinessAttribute> categories = DatabaseReader.loadCategories(conn, RESTAURANTS_ONLY);
		for(int i = 0; i < categories.size(); ++i) {
			BusinessAttribute category = categories.get(i);
			businesses[businessMap.convert(category.business_id)].addAttribute(category.name, category.value);
		}
	}

	public void loadTrainingRatings() {
		System.out.println("Begin loading training data");
		ArrayList<UserBusinessInteraction> trainingData = DatabaseReader.loadTrainingData(conn, USE_SQLITE, RESTAURANTS_ONLY);
		for(UserBusinessInteraction data : trainingData) {
			users[userMap.convert(data.userID)].addRating(data.businessID, data.rating);
		}
	}

	public void loadTestingRatings() {
		System.out.println("Begin loading testing data");
		testingData = DatabaseReader.loadTestingData(conn, USE_SQLITE, RESTAURANTS_ONLY);
	}

	public boolean loadModel() {
		System.out.println("Begin loading model");
		String filepath = DatabaseReader.getLatestModel(conn);
		if(filepath == null) {
			System.out.println("Cancel loading model");
			return false;
		}

		//deserialize
		model = null;
		try {   
			// Reading the object from a file
			FileInputStream file = new FileInputStream(filepath);
			ObjectInputStream in = new ObjectInputStream(file);

			// Method for deserialization of object
			model = (Model) in.readObject();
			numIters = model.iters;

			in.close();
			file.close();
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		numIters = model.iters;
		return true;
	}

	public void createModel() {
		System.out.println("Begin creating new model");
		numIters = 0;
		model = new Model();
	}

	public boolean saveModel() {
		System.out.println("Begin saving model");
		String filepath = RESTAURANTS_ONLY ? String.format("model_rest_%d.ser", numIters) : String.format("model_%d.ser", numIters);
		// Serialization 
		try{   
			//Saving of object in a file
			FileOutputStream file = new FileOutputStream(filepath);
			ObjectOutputStream out = new ObjectOutputStream(file);

			// Method for serialization of object
			out.writeObject(model);

			out.close();
			file.close();
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		return DatabaseReader.saveModel(conn, numIters, filepath);
	}

	public boolean saveModel(double rmse) {
		System.out.println("Begin saving model");
		String filepath = RESTAURANTS_ONLY ? String.format("model_rest_%d.ser", numIters) : String.format("model_%d.ser", numIters);
		// Serialization 
		try{   
			//Saving of object in a file
			FileOutputStream file = new FileOutputStream(filepath);
			ObjectOutputStream out = new ObjectOutputStream(file);

			// Method for serialization of object
			out.writeObject(model);

			out.close();
			file.close();
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		return DatabaseReader.saveModel(conn, numIters, filepath, rmse);
	}
	
	public void train() {
		if(numIters == 0) {
			System.out.println("Begin training");
			double rmse = rmse();
			System.out.printf("finished iter %4d. rmse: %f\n", numIters, rmse);
			saveModel(rmse);
		} else {
			System.out.println("Resume training");
		}

		while(numIters < 1000000) {
			sgd();
			double rmse = rmse();
			System.out.printf("finished iter %4d. rmse: %f\n", numIters, rmse);
			saveModel(rmse);
		}
	}

	public void sgd() {
		System.out.println("Begin new training iteration");
		double eta = 0.0000001;
		Random rand = new Random();
		//does 1000000 mini-iterations for every one large-iteration
		for(int i = 0; i < 1000000; ) {
			int userIndex = rand.nextInt(numUsers);
			User user = users[userIndex];
			if(user.numRatings() < 2) {
				continue;
			}
			double[][] assembledSimilarityMatrix = Helper.zeros(NUM_STARS, NUM_FEATURES);
			UserBusinessInteraction[] interactions = user.getInteractions();
			int businessIndex = rand.nextInt(interactions.length);
			Business business = businesses[businessMap.convert(interactions[businessIndex].businessID)];
			int thisRating = interactions[businessIndex].rating;
			for(int j = 0; j < interactions.length; ++j) {
				if(j == businessIndex) continue;
				Business business2 = businesses[businessMap.convert(interactions[j].businessID)];
				int rating = interactions[j].rating;
				Helper.inplaceAddRow(assembledSimilarityMatrix, business.similarity(business2), rating-1);
			}
			model.sgd(assembledSimilarityMatrix, thisRating-1, eta);
			++i;
		}
		++numIters;
		++model.iters;
	}

	public double rmse() {
		System.out.println("Begin calculating rmse");
		double squaredErrorSum = 0;
		for(int i = 0; i < testingData.size(); ++i) {
			double[][] assembledSimilarityMatrix = Helper.zeros(NUM_STARS, NUM_FEATURES);
			User user = users[userMap.convert(testingData.get(i).userID)];
			Business business = businesses[businessMap.convert(testingData.get(i).businessID)];
			int thisRating = testingData.get(i).rating;
			UserBusinessInteraction[] interactions = user.getInteractions();
			for(int j = 0; j < interactions.length; ++j) {
				Business business2 = businesses[businessMap.convert(interactions[j].businessID)];
				int rating = interactions[j].rating;
				Helper.inplaceAddRow(assembledSimilarityMatrix, business.similarity(business2), rating-1);
			}
			model.feedforward(assembledSimilarityMatrix);
			double prediction = model.numericalOutput();
			double diff = thisRating - prediction;
			squaredErrorSum += (diff*diff);
		}
		return Math.sqrt(squaredErrorSum / testingData.size());
	}
	
	public void recommend() {
		Scanner scan = new Scanner(System.in);
		Random rand = new Random();
		System.out.println("Hello. Welcome to \"Combining Mining Techniques to Improve Business Recommendation with Yelp Database\".");
		System.out.println("Please enter your 22 character id to begin, or \"unknown\" if you don't know it.");
		System.out.print(": ");
		String userID = scan.next();
		scan.close();
		if(userID.equalsIgnoreCase("unknown")){
			int choice = rand.nextInt(numUsers);
			userID = userMap.convert(choice);
			System.out.printf("A random id, %s, has been chosen for this test.", userID);
		} else if(!userMap.contains(userID)) {
			System.out.println("That id was not found in the database.");
			int choice = rand.nextInt(numUsers);
			userID = userMap.convert(choice);
			System.out.printf("A random id, %s, has been chosen for this test.", userID);
		} else {
			System.out.println("Welcome!");
		}
		ArrayList<UserBusinessInteraction> interactions = DatabaseReader.loadReviewsForUser(conn, userID);
		if(interactions.isEmpty()) { //users without any reviews shouldnt exist but just in case
			System.out.println("It appears that you haven't reviewed any businesses.");
			System.out.println("Review some businesses and try again.");
			return;
		}
		System.out.printf("It appears that you have reviewed %d businesses.\n", interactions.size());
		for(int i = 0; i < interactions.size(); ++i) {
			UserBusinessInteraction interaction = interactions.get(i);
			Business business = businesses[businessMap.convert(interaction.businessID)];
			System.out.printf("\t%d stars: %s\n", interaction.rating, business.name);
		}
	}

	/**
	 * Main method.
	 * @param args ignored
	 */
	public static void main(String[] args) {
		System.out.println("Program start");
		
		boolean train = true;
		//System.out.printf("args.length: %d\n", args.length);
		for(int i = 0; i < args.length; ++i) {
			//System.out.printf("args[%d]: %s\n", i, args[i]);
			if(args[i].equals("-r")) train = false;
		}
		RecommenderSystem recSys = new RecommenderSystem();

		if(train) 	recSys.train();
		else 		recSys.recommend();

		System.out.println("Program end");
	}
}
