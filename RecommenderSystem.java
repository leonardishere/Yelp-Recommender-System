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
    public NeuralNetwork net = null;
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
        //boolean loadedModel = loadModel();
        //if(!loadedModel) createModel();
        //loadModel("model_rest_632.ser");
        
        boolean loadedNet = loadNet();
        if(!loadedNet) createNet();
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

    public boolean loadModel(String filepath) {
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
    
    public boolean loadNet() {
        System.out.println("Begin loading net");
        String filepath = DatabaseReader.getLatestNet(conn);
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
            net = (NeuralNetwork) in.readObject();
            numIters = net.iters;

            in.close();
            file.close();
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        //numIters = net.iters;
        return true;
    }

    public boolean loadNet(String filepath) {
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

    public void createNet() {
        System.out.println("Begin creating new net");
        numIters = 0;
        net = new NeuralNetwork(new Layer[] {
            new InputLayer(NUM_STARS*NUM_FEATURES),
            new SigmoidLayer(100),
            new SigmoidLayer(100),
            new SigmoidLayer(100),
            new LinearOutputLayer()
        });
    }
    
    public boolean saveNet(double rmse) {
        System.out.println("Begin saving model");
        String filepath = RESTAURANTS_ONLY ? String.format("net_rest_%d.ser", numIters) : String.format("net_%d.ser", numIters);
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
            //double rmse = approxRmse(100);
            System.out.printf("finished iter %4d. rmse: %f\n", numIters, rmse);
            saveModel(rmse);
        }
    }

    public void sgd() {
        System.out.println("Begin new training iteration");
        double eta = 0.00001;
        Random rand = new Random();
        //does 1000000 mini-iterations for every one large-iteration
        //for(int i = 0; i < 1000000; ) {
        for(int i = 0; i < 10000; ) {
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
                double[] similarity = business.similarity(business2);
                int rating = interactions[j].rating;
                //if(rating == thisRating) similarity = Helper.svMul(2, similarity);
                Helper.inplaceAddRow(assembledSimilarityMatrix, similarity, rating-1);
            }
            //model.sgd(assembledSimilarityMatrix, thisRating-1, eta);
            net.sgd(Helper.flatten(assembledSimilarityMatrix), thisRating, eta);
            ++i;
        }
        ++numIters;
        //++model.iters;
        ++net.iters;
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
                double[] similarity = business.similarity(business2);
                int rating = interactions[j].rating;
                //if(rating == thisRating) similarity = Helper.svMul(2, similarity);
                Helper.inplaceAddRow(assembledSimilarityMatrix, similarity, rating-1);
            }
            //model.feedforward(assembledSimilarityMatrix);
            //double prediction = model.numericalOutput();
            net.feedforward(Helper.flatten(assembledSimilarityMatrix));
            double prediction = net.getOutput();
            double diff = thisRating - prediction;
            squaredErrorSum += (diff*diff);
        }
        return Math.sqrt(squaredErrorSum / testingData.size());
    }

    public double approxRmse(int count) {
        System.out.println("Begin calculating approx rmse");
        double squaredErrorSum = 0;
        Random rand = new Random();
        //for(int i = 0; i < testingData.size(); ++i) {
        for(int c = 0; c < count; ++c) {
            int i = rand.nextInt(testingData.size());
            double[][] assembledSimilarityMatrix = Helper.zeros(NUM_STARS, NUM_FEATURES);
            User user = users[userMap.convert(testingData.get(i).userID)];
            Business business = businesses[businessMap.convert(testingData.get(i).businessID)];
            int thisRating = testingData.get(i).rating;
            UserBusinessInteraction[] interactions = user.getInteractions();
            for(int j = 0; j < interactions.length; ++j) {
                Business business2 = businesses[businessMap.convert(interactions[j].businessID)];
                double[] similarity = business.similarity(business2);
                int rating = interactions[j].rating;
                //if(rating == thisRating) similarity = Helper.svMul(2, similarity);
                Helper.inplaceAddRow(assembledSimilarityMatrix, similarity, rating-1);
            }
            //model.feedforward(assembledSimilarityMatrix);
            //double prediction = model.numericalOutput();
            net.feedforward(Helper.flatten(assembledSimilarityMatrix));
            double prediction = net.getOutput();
            double diff = thisRating - prediction;
            squaredErrorSum += (diff*diff);
        }
        return Math.sqrt(squaredErrorSum / count);
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

    public void printValues() {
        double[][] weights = model.weights;
        double[] biases = model.biases;
        /*
        for(int i = 0; i < NUM_STARS; ++i) {
            System.out.printf("%d: ", i+1);
            for(int j = 0; j < Business.attributes.size(); ++j) {
                System.out.printf("\t%30s: %9.6f\n", Business.attributes.get(j), weights[i][j]);
            }
            System.out.printf("\t%30s: %9.6f\n", "Category", weights[i][39]);
            System.out.printf("\t%30s: %9.6f\n", "Term Frequency", weights[i][40]);
            System.out.printf("\t%30s: %9.6f\n", "Bias", biases[i]);
            System.out.println("");
        }
        */
        System.out.println("weights:");
        for(int i = 0; i < weights.length; ++i) {
            System.out.print("{");
            for(int j = 0; j < weights[i].length; ++j) {
                System.out.print(weights[i][j]);
                if(j != weights[i].length-1) System.out.print(",");
            }
            System.out.println("}");
        }
        System.out.print("biases:\n{");
        for(int i = 0; i < biases.length; ++i) {
            System.out.print(biases[i]);
            if(i != biases.length-1) System.out.print(",");
        }
        System.out.print("}");
    }
    
    public void fakeItTilYouMakeIt() {
        double[][] weights = {
            {0.022606338691446248,0.06533943124122969,0.0018972969234822374,0.03582740934471229,-0.03084289277177142,0.05381432672490075,0.0023846116542867173,0.043233128980664795,-0.006679497847269348,0.028480159986364606,0.002108230927712974,0.01817094232131519,-7.115862197049922E-4,0.011583513857620298,0.003961881797768408,0.08685817843154268,0.00759348198791246,0.07206534078133316,0.002286311172955392,0.09886958992498646,-0.017616648297034364,0.0660713263495437,-0.0061805923476642355,0.0019729704753037925,-7.757854659902689E-4,0.07328334237153669,0.0039829987869906686,0.08994195198545747,0.009218287782292353,0.05342806768954057,-0.005444899035816773,0.0772430349506257,0.00442666477831635,0.01655716149779123,0.00983227151853275,0.00654477059753108,-0.0016257434204008901,0.060556586254078,0.05013255465153772,0.06507723897700693,0.03098776261206745},
            {-0.010396039535967792,0.08514268083916353,0.005846084154672033,0.05897977009374507,0.035769943252401126,0.0023913812627080148,0.001105565393175046,0.05665770756587048,0.0012307199863773408,0.0996065374306634,9.376675023589537E-4,0.013789609079341791,-0.00189148586427685,0.05905250146315116,0.005037804235367439,0.014272325373057638,0.0038902716201632834,0.09295943217495123,0.006030290688709837,0.01794795372025948,0.0015414280444178321,0.06592989583365481,-0.011057731230963262,0.012825600709729247,-0.02287208168356413,0.04594929169587236,0.0044450853050361,0.0739541280032706,0.0086271656288184,0.09627124672634611,-0.004794049635813348,0.07114870987221154,9.212789959208998E-5,0.09671861586610002,0.006550762727559045,0.039355923276188726,0.0024897045632021253,0.07483526092709451,0.09267936584459557,0.04122500905014532,0.009226187520252593},
            {-0.03360468384249066,0.030504685279117608,0.0048912914110053225,0.08733319565013449,0.054204138782824866,0.07302309650412438,0.0014798084707217526,0.04593766544137943,0.0013812784348629092,0.09112777475574924,0.004045382448797041,0.07470539022008209,8.6139744490643E-4,0.04265371342443624,0.003750018357500533,0.0016145081171115795,0.0049727595093657425,0.04988964371666449,-0.0025223549697423217,0.0873084193693277,-0.00593600355160865,0.04042051197753043,-0.0010876830813606833,0.07016478738361222,-0.00921540778426174,0.041037445584891113,0.0061747119125983374,0.08995238464662109,0.003251346705906307,0.03837794279044272,-0.004929957959490252,0.0996857358022926,0.006385280713045518,0.0845097671622286,0.010264421237185243,0.04765081140090391,0.002269267631974796,0.03084783258593209,0.0631920066990373,0.05512610605631637,0.07103631618644163},
            {-0.022765583638135398,0.06673693814375263,0.003464353994629447,0.03659331000245546,0.02232393354928728,0.002488037412838795,-7.455481660248482E-4,0.05847515251152177,0.009811916467275188,0.043310842182642376,0.001679476676654681,0.04527252601518224,0.007177986617841974,0.0014800792181458246,0.0038219825210175184,0.08335696703126472,-0.0038005165713835398,0.04943081109171972,6.988588794580065E-4,0.013267340988914956,0.009831213310411828,0.08016537456977092,-0.0026569530056947646,0.083833442844731,0.011609839803992881,0.03145246140777772,0.002807421565281453,0.0904484969189411,0.007514076961838339,0.016411693172901122,-0.0011072838809921045,0.09267179383096773,0.004511728624870567,0.08715915365227583,0.009149997938297525,0.010737319032530335,0.005787660336638239,0.007896280792037691,0.07301392078608741,0.009824388713146427,0.04126433683748955},
            {-0.01600975813058331,0.006164161771296518,-0.001347267959062006,0.07646393848435734,0.004356498097987621,0.023038766689831902,-0.01134942996470128,0.027676666477125435,0.01794353646735371,0.05669114627686306,0.0041195557077153815,0.015014899934875104,0.010824455938919347,0.04732214314651906,0.004481351248810496,0.09675368268361823,-0.007889461799004613,0.006344612258750504,0.005024340512362985,0.0834554430880477,0.011794304985192442,0.03137165745077606,0.0023205473313497134,0.05556503765496071,0.02200550784387617,0.05351580034219203,0.0029889629670898517,0.007903463352877393,-0.006395743864672655,0.09262475274906679,0.0013865703976205508,0.02153937152607276,0.004743842872428498,0.08094263174497168,0.008661674052092718,0.08119327334159056,0.004761865364982198,0.09117382385912765,0.024065962606022853,-0.01874018505164533,0.07525478128812219}
        };
        double[] biases = {0.03468877481508883,0.029504973889329593,0.03396794899212473,0.07434076082370744,0.11920847791129348};
        Model2 model2 = new Model2(weights, biases);
        
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
                double[] similarity = business.similarity(business2);
                int rating = interactions[j].rating;
                //if(rating == thisRating) similarity = Helper.svMul(2, similarity);
                Helper.inplaceAddRow(assembledSimilarityMatrix, similarity, rating-1);
            }
            model.feedforward(assembledSimilarityMatrix);
            double prediction = model2.numericalOutput();
            double diff = thisRating - prediction;
            squaredErrorSum += (diff*diff);
        }
        double rmse = Math.sqrt(squaredErrorSum / testingData.size());
        
        System.out.printf("RMSE with altered model: %f\n", rmse);
    }

    /**
     * Main method.
     * @param args ignored
     */
    public static void main(String[] args) {
        System.out.println("Program start");

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
        RecommenderSystem recSys = new RecommenderSystem();
        //printValues = true;
        //recSys.fakeItTilYouMakeIt();
        
        if(train) recSys.train();
        if(printValues) recSys.printValues();
        if(recommend) recSys.recommend();

        System.out.println("Program end");
    }
}
