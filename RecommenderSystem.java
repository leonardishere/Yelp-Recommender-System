import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Random;

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
	public static final String SQLITE_YELP_DB_URL = "jdbc:sqlite:C:/sqlite/db/yelp.db";
	public static final boolean USE_SQLITE = false; //as opposed to mysql
	public Connection conn = null;

	public RecommenderSystem() {
		conn = USE_SQLITE ? DatabaseReader.connect_sqlite() : DatabaseReader.connect_mysql();
		loadUsers();
		loadBusinesses();
		loadTrainingRatings();
		boolean loadedModel = loadModel();
		if(!loadedModel) createModel();
	}
	
	public void loadUsers() {
		System.out.println("Begin loading users");
		users = DatabaseReader.loadUsers(conn);
		userMap = new KeyMap();
		numUsers = users.length;
		for(int i = 0; i < numUsers; ++i) {
			userMap.add(users[i].id);
		}
	}
	
	public void loadBusinesses() {
		System.out.println("Begin loading businesses");
		businesses = DatabaseReader.loadBusinesses(conn);
		businessMap = new KeyMap();
		numBusinesses = businesses.length;
		for(int i = 0; i < numBusinesses; ++i) {
			businessMap.add(businesses[i].id);
		}
	}

	public void loadTrainingRatings() {
		ArrayList<UserBusinessInteraction> trainingData = DatabaseReader.loadTrainingData(conn, USE_SQLITE);
		for(UserBusinessInteraction data : trainingData) {
			users[userMap.convert(data.userID)].addRating(data.businessID, data.rating);
		}
	}
	
	public void loadTestingRatings() {
		testingData = DatabaseReader.loadTestingData(conn, USE_SQLITE);
	}
	
	public boolean loadModel() {
		String filepath = DatabaseReader.getLatestModel(conn);
        if(filepath == null) return false;
        
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
        return true;
	}
	
	public void createModel() {
		numIters = 0;
		model = new Model();
	}
	
	public boolean saveModel() {
    	String filepath = String.format("model_%d.ser", numIters);
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
	
	public void train() {
		if(numIters == 0) {
			double rmse = rmse();
			System.out.printf("finished iter %4. rmse: %f\n", numIters, rmse);
		}
		
		double eta = 0.0001;
		Random rand = new Random();
		//does 1000 mini-iterations for every one large-iteration
		for(int i = 0; i < 1000; ) {
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
		double rmse = rmse();
		System.out.printf("finished iter %4. rmse: %f\n", numIters, rmse);
	}
	
	public double rmse() {
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

	/**
	 * Main method.
	 * @param args ignored
	 */
	public static void main(String[] args) {
		System.out.println("Program start");
		RecommenderSystem recSys = new RecommenderSystem();
		while(recSys.numIters < 1000) {
			recSys.train();
		}
		System.out.println("Program end");
	}
}
