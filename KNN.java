import java.sql.Connection;
import java.util.ArrayList;

public class KNN {
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
    public static final boolean USE_SQLITE = true; //as opposed to mysql
    public static final boolean RESTAURANTS_ONLY = true; //as opposed to all businesses
    public Connection conn = null;
    public static final String SQLITE_URL = "jdbc:sqlite:/local/weka/yelp.db";
    public static final String SQLITE_RESTAURANT_URL = "jdbc:sqlite:/local/weka/yelp_restaurants.db";
    public static final String SQLITE_ACTIVE_LIFE_URL = "jdbc:sqlite:/local/weka/yelp_activelife.db";
    
    public KNN() {
        //conn = DatabaseReader.connect_sqlite(SQLITE_URL);
        conn = DatabaseReader.connect_sqlite(SQLITE_RESTAURANT_URL);
        //conn = DatabaseReader.connect_sqlite(SQLITE_ACTIVE_LIFE_URL);
        
        loadUsers();
        loadBusinesses();
        loadAttributes();
        loadCategories();
        loadTrainingRatings();
        loadTestingRatings();
        //boolean loadedModel = loadModel();
        //if(!loadedModel) createModel();
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

    public void loadAttributes() {
        System.out.println("Begin loading attributes");
        ArrayList<BusinessAttribute> attributes = DatabaseReader.loadAttributes(conn);
        for(int i = 0; i < attributes.size(); ++i) {
            BusinessAttribute attribute = attributes.get(i);
            businesses[businessMap.convert(attribute.business_id)].addAttribute(attribute.name, attribute.value);
        }
    }

    public void loadCategories() {
        System.out.println("Begin loading categories");
        ArrayList<BusinessAttribute> categories = DatabaseReader.loadCategories(conn);
        for(int i = 0; i < categories.size(); ++i) {
            BusinessAttribute category = categories.get(i);
            businesses[businessMap.convert(category.business_id)].addAttribute(category.name, category.value);
        }
    }

    public void loadTrainingRatings() {
        System.out.println("Begin loading training data");
        ArrayList<UserBusinessInteraction> trainingData = DatabaseReader.loadTrainingData(conn, USE_SQLITE);
        for(UserBusinessInteraction data : trainingData) {
            users[userMap.convert(data.userID)].addRating(data.businessID, data.rating);
        }
    }

    public void loadTestingRatings() {
        System.out.println("Begin loading testing data");
        testingData = DatabaseReader.loadTestingData(conn, USE_SQLITE);
    }

    public double rmse() {
        System.out.println("Begin calculating rmse");
        double squaredErrorSum = 0;
        for(int i = 0; i < testingData.size(); ++i) {
            User user = users[userMap.convert(testingData.get(i).userID)];
            Business business = businesses[businessMap.convert(testingData.get(i).businessID)];
            int thisRating = testingData.get(i).rating;
            double prediction = predict(user, business);

            double diff = thisRating - prediction;
            squaredErrorSum += (diff*diff);
        }
        return Math.sqrt(squaredErrorSum / testingData.size());
    }
    
    /**
     * Returns the closest Businesses that a User has rated to the given Business.
     */
    public double predict(User user, Business business){
        UserBusinessInteraction[] interactions = user.getInteractions();
        double sumMuls = 0;
        double sumSims = 0;
        for(int i = 0; i < interactions.length; ++i) {
            UserBusinessInteraction interaction = interactions[i];
            Business business2 = businesses[businessMap.convert(interaction.businessID)];
            double similarity = Helper.length(business.similarity(business2));
            sumMuls += interaction.rating * similarity;
            sumSims += similarity;
        }
        if(sumSims > 0) return sumMuls /sumSims;
        else return 3; //most often
    }
    
    /**
     * Returns the closest Businesses that a User has rated to the given Business.
     */
    public static double predict(User user, Business business, Business[] businesses, KeyMap businessMap){
        UserBusinessInteraction[] interactions = user.getInteractions();
        double sumMuls = 0;
        double sumSims = 0;
        for(int i = 0; i < interactions.length; ++i) {
            UserBusinessInteraction interaction = interactions[i];
            Business business2 = businesses[businessMap.convert(interaction.businessID)];
            double similarity = Helper.length(business.similarity(business2));
            sumMuls += interaction.rating * similarity;
            sumSims += similarity;
        }
        if(sumSims > 0) return sumMuls /sumSims;
        else return 3; //most often
    }

    /**
     * Main method.
     * @param args ignored
     */
    public static void main(String[] args) {
        System.out.println("Program start");

        /*
        boolean train = false;
        boolean printValues = false;
        boolean recommend = false;
        //System.out.printf("args.length: %d\n", args.length);
        for(int i = 0; i < args.length; ++i) {
            //System.out.printf("args[%d]: %s\n", i, args[i]);
            if(args[i].equals("-t")) train = true;
            if(args[i].equals("-p")) printValues = true;
            if(args[i].equals("-r")) recommend = true;
        }
        */
        KNN knn = new KNN();
        double rmse = knn.rmse();
        System.out.printf("finished knn analysis. rmse: %f\n", rmse);

        /*
        if(train) recSys.train();
        if(printValues) recSys.printValues();
        if(recommend) recSys.recommend();
        */
        System.out.println("Program end");
    }
}
