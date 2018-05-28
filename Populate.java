

import org.json.*;


import java.io.*;
import java.sql.*;
import java.util.*;

/**
*Code from COEN 280 for parsing the json objects into SQL statements
**/


public class Populate {

   public static ArrayList<String> SQLInsertStatements;

    public Populate() {
        this.SQLInsertStatements = new ArrayList<String>();
    }

    public void populateMainCategories(){

        SQLInsertStatements.add("INSERT INTO MainCategories (MainCategoryID, CategoryName) Values(1,'Active Life')");
        SQLInsertStatements.add("INSERT INTO MainCategories (MainCategoryID, CategoryName) Values(2,'Arts & Entertainment')");
        SQLInsertStatements.add("INSERT INTO MainCategories (MainCategoryID, CategoryName) Values(3,'Automotive')");
        SQLInsertStatements.add("INSERT INTO MainCategories (MainCategoryID, CategoryName) Values(4,'Car Rental')");
        SQLInsertStatements.add("INSERT INTO MainCategories (MainCategoryID, CategoryName) Values(5,'Cafes')");
        SQLInsertStatements.add("INSERT INTO MainCategories (MainCategoryID, CategoryName) Values(6,'Beauty & Spas')");
        SQLInsertStatements.add("INSERT INTO MainCategories (MainCategoryID, CategoryName) Values(7,'Convenience Stores')");
        SQLInsertStatements.add("INSERT INTO MainCategories (MainCategoryID, CategoryName) Values(8,'Dentists')");
        SQLInsertStatements.add("INSERT INTO MainCategories (MainCategoryID, CategoryName) Values(9,'Doctors')");
        SQLInsertStatements.add("INSERT INTO MainCategories (MainCategoryID, CategoryName) Values(10,'Drugstores')");
        SQLInsertStatements.add("INSERT INTO MainCategories (MainCategoryID, CategoryName) Values(11,'Department Stores')");
        SQLInsertStatements.add("INSERT INTO MainCategories (MainCategoryID, CategoryName) Values(12,'Education')");
        SQLInsertStatements.add("INSERT INTO MainCategories (MainCategoryID, CategoryName) Values(13,'Event Planning & Services')");
        SQLInsertStatements.add("INSERT INTO MainCategories (MainCategoryID, CategoryName) Values(14,'Flowers & Gifts')");
        SQLInsertStatements.add("INSERT INTO MainCategories (MainCategoryID, CategoryName) Values(15,'Food')");
        SQLInsertStatements.add("INSERT INTO MainCategories (MainCategoryID, CategoryName) Values(16,'Health & Medical')");
        SQLInsertStatements.add("INSERT INTO MainCategories (MainCategoryID, CategoryName) Values(17,'Home Services')");
        SQLInsertStatements.add("INSERT INTO MainCategories (MainCategoryID, CategoryName) Values(18,'Home & Garden')");
        SQLInsertStatements.add("INSERT INTO MainCategories (MainCategoryID, CategoryName) Values(19,'Hospitals')");
        SQLInsertStatements.add("INSERT INTO MainCategories (MainCategoryID, CategoryName) Values(20,'Hotels & Travel')");
        SQLInsertStatements.add("INSERT INTO MainCategories (MainCategoryID, CategoryName) Values(21,'Hardware Stores')");
        SQLInsertStatements.add("INSERT INTO MainCategories (MainCategoryID, CategoryName) Values(22,'Grocery')");
        SQLInsertStatements.add("INSERT INTO MainCategories (MainCategoryID, CategoryName) Values(23,'Medical Centers')");
        SQLInsertStatements.add("INSERT INTO MainCategories (MainCategoryID, CategoryName) Values(24,'Nurseries & Gardening')");
        SQLInsertStatements.add("INSERT INTO MainCategories (MainCategoryID, CategoryName) Values(25,'Nightlife')");
        SQLInsertStatements.add("INSERT INTO MainCategories (MainCategoryID, CategoryName) Values(26,'Restaurants')");
        SQLInsertStatements.add("INSERT INTO MainCategories (MainCategoryID, CategoryName) Values(27,'Shopping')");
        SQLInsertStatements.add("INSERT INTO MainCategories (MainCategoryID, CategoryName) Values(28,'Transportation')");

        executeInsertJDBC();
    }

    public String replaceSpecialCharacters(String str) {
        String text =  new String(str.replace('\'', '\\'));
        if (text.length() > 255) {
            return text.substring(0,254);
        }
        return text;
    }

    //Parse a User
    public void parseUser(JSONObject jsonObject) {

        String UserID = (String)jsonObject.get("user_id");
        String yelpingSince = (String)jsonObject.get("yelping_since");
        Integer funnyVotes = (Integer)jsonObject.getJSONObject("votes").get("funny");
        Integer usefulVotes = (Integer) jsonObject.getJSONObject("votes").get("useful");
        Integer coolVotes = (Integer) jsonObject.getJSONObject("votes").get("cool");

        Integer ReviewCount = (Integer)jsonObject.get("review_count");
        String Name = (String)jsonObject.get("name");
        Integer fans = (Integer)jsonObject.get("fans");
        Double averageStars = (Double)jsonObject.get("average_stars");

        String sqlStatement = "INSERT INTO YelpUser(UserID, YelpingSince, FunnyVotes, UsefulVotes, CoolVotes," +
                "ReviewCount, UserName, Fans, AverageStars)" + " VALUES('" + UserID +"','"+ yelpingSince + "',"+
                funnyVotes + ","+usefulVotes + ","+coolVotes + ","+ReviewCount + ", '"+ replaceSpecialCharacters(Name) + "',"+fans + ","+ averageStars +")";

        SQLInsertStatements.add(sqlStatement);

    }

    //Parse the attributes since they're in a nested object they need special treatment
    public void parseAttributes(JSONObject attributes, String parent_object, String BusinessID) {

        Iterator<?> keys = attributes.keys();

        if (!parent_object.equals("")) {
            parent_object = parent_object.replace(" ","_") + "_";
        }

        while( keys.hasNext() ) {
            String key = (String)keys.next();
            if ( attributes.get(key) instanceof JSONObject ) {
                parseAttributes((JSONObject)attributes.get(key), key, BusinessID);
            }
            else {
                String temp = parent_object+key.replace(" ", "_") + "_" + attributes.get(key).toString().replace(" ","_");
                SQLInsertStatements.add("INSERT INTO BusinessHasAttributes(BusinessID, AttributeName) VALUES('" + BusinessID +"','"+temp +"')");
            }

        }
    }

    //Count the number of checkins for a specific business
    public int countCheckins(JSONObject checkins) {

        Iterator<?> keys = checkins.keys();
        int numberOfCheckins = 0;

        while( keys.hasNext() ) {
            String key = (String) keys.next();
            numberOfCheckins += (Integer)checkins.get(key);
        }
        return numberOfCheckins;
    }

    //Parse a Business Object from Json
    public void parseBusiness(JSONObject jsonObject) {

        //get business information
        String BusinessID = (String)jsonObject.get("business_id");
        String FullAddress = (String)jsonObject.get("full_address");
        Boolean Open = (Boolean)jsonObject.get("open");
        String City = (String)jsonObject.get("city");
        String State = (String)jsonObject.get("state");
        Double Latitude = (Double)jsonObject.get("latitude");
        Double Longitude = (Double)jsonObject.get("longitude");
        Integer ReviewCount = (Integer)jsonObject.get("review_count");
        String BusinessName = (String)jsonObject.get("name");
        Double Stars = (Double)jsonObject.get("stars");

        Map<String, String> hours = new HashMap<String, String>();

        //Extract info regarding business hours
        JSONObject businessHours = jsonObject.getJSONObject("hours");
        if (businessHours.has("Monday")) {
            hours.put("MondayOpen", (String)businessHours.getJSONObject("Monday").get("open"));
            hours.put("MondayClose", (String)businessHours.getJSONObject("Monday").get("close"));
        }
        if (businessHours.has("Tuesday")) {
            hours.put("TuesdayOpen", (String)businessHours.getJSONObject("Tuesday").get("open"));
            hours.put("TuesdayClose", (String)businessHours.getJSONObject("Tuesday").get("close"));
        }
        if (businessHours.has("Wednesday")) {
            hours.put("WednesdayOpen", (String)businessHours.getJSONObject("Wednesday").get("open"));
            hours.put("WednesdayClose", (String)businessHours.getJSONObject("Wednesday").get("close"));
        }
        if (businessHours.has("Thursday")) {
            hours.put("ThursdayOpen", (String)businessHours.getJSONObject("Thursday").get("open"));
            hours.put("ThursdayClose", (String)businessHours.getJSONObject("Thursday").get("close"));
        }
        if (businessHours.has("Friday")) {
            hours.put("FridayOpen", (String)businessHours.getJSONObject("Friday").get("open"));
            hours.put("FridayClose", (String)businessHours.getJSONObject("Friday").get("close"));
        }
        if (businessHours.has("Saturday")) {
            hours.put("SaturdayOpen", (String)businessHours.getJSONObject("Saturday").get("open"));
            hours.put("SaturdayClose", (String)businessHours.getJSONObject("Saturday").get("close"));
        }
        if (businessHours.has("Sunday")) {
            hours.put("SundayOpen", (String)businessHours.getJSONObject("Sunday").get("open"));
            hours.put("SundayClose", (String)businessHours.getJSONObject("Sunday").get("close"));
        }

        JSONArray categories = jsonObject.getJSONArray("categories");

        //Create business information insert statement
        String sqlStatement1 = "INSERT INTO Business(BusinessID, FullAddress, BusinessOpen, City, AddrState," +
                "Latitude, Longitude, ReviewCount, BusinessName, CheckinCount, Stars) VALUES('" + BusinessID +"','"+ replaceSpecialCharacters(FullAddress) + "','"+
                Open.toString() + "','"+City + "','"+State + "',"+Latitude + ","+Longitude + ","+ReviewCount + ",'"+ replaceSpecialCharacters(BusinessName) +"', 0," +Stars+")";
        SQLInsertStatements.add(sqlStatement1);

        JSONObject jObject = (JSONObject)jsonObject.get("attributes");
        parseAttributes(jObject, new String(), BusinessID);

        //Create BusinessHasHours statement
        String sqlStatement2 = "INSERT INTO BusinessHasHours(BusinessID, MondayOpen, MondayClose," +
                "TuesdayOpen, TuesdayClose, WednesdayOpen, WednesdayClose, ThursdayOpen, ThursdayClose," +
                "FridayOpen, FridayClose, SaturdayOpen, SaturdayClose, SundayOpen, SundayClose) VALUES( '" + BusinessID + "','" +
                hours.get("MondayOpen") + "','" + hours.get("MondayClose") +
                        "','" + hours.get("TuesdayOpen") + "','" + hours.get("TuesdayClose") + "','" + hours.get("WednesdayOpen") +
                        "','" + hours.get("WednesdayClose") + "','" + hours.get("ThursdayOpen") + "','" + hours.get("ThursdayClose") +
                        "','" + hours.get("FridayOpen") + "','" + hours.get("FridayClose") + "','" + hours.get("SaturdayOpen") +
                        "','" + hours.get("SaturdayClose") + "','" + hours.get("SundayOpen") + "','" + hours.get("SundayClose")+"')";

        SQLInsertStatements.add(sqlStatement2);

        String temp = "UPDATE Business SET Stars=" + Stars + "WHERE BusinessID='" + BusinessID+"'";
        SQLInsertStatements.add(temp);

        List<Object> categoriesList = categories.toList();
        for(int i=0; i< categoriesList.size(); i++) {
            String category = (String)categoriesList.get(i);
            SQLInsertStatements.add("INSERT INTO BusinessHasCategories(BusinessID, CategoryName)" +
                    "VALUES('" + BusinessID + "','" + replaceSpecialCharacters(category) + "')");
        }

    }

    public void parseReview(JSONObject jsonObject) {
        String UserID = (String)jsonObject.get("user_id");
        String ReviewID = (String)jsonObject.get("review_id");
        Integer stars = (Integer)jsonObject.get("stars");
        String Date = (String)jsonObject.get("date");
        String text = (String)jsonObject.get("text");
        String BusinessID = (String)jsonObject.get("business_id");
        Integer funnyVotes = (Integer)jsonObject.getJSONObject("votes").get("funny");
        Integer usefulVotes = (Integer) jsonObject.getJSONObject("votes").get("useful");
        Integer coolVotes = (Integer) jsonObject.getJSONObject("votes").get("cool");

        try {
            String sqlString = "INSERT INTO Review(UserID, ReviewID, Stars, DatePublished, Text," +
                    "BusinessID, UsefulVotes, FunnyVotes, CoolVotes) VALUES('" + UserID +"','"+ ReviewID + "',"+
                    stars + ",'"+ Date + "','"+ replaceSpecialCharacters(text) + "','"+ BusinessID + "',"+ usefulVotes + ","+funnyVotes + ","+ coolVotes +")";

           SQLInsertStatements.add(sqlString);
        }
        catch(Exception e) {
            System.out.println("Error creating Review " +ReviewID);
        }
    }

    public void parseCheckin(JSONObject jsonObject) {
        String BusinessID = (String)jsonObject.get("business_id");


        try {
            int checkinNum = countCheckins(jsonObject.getJSONObject("checkin_info"));
            String sqlString = "UPDATE BUSINESS B SET B.CheckinCount= B.CheckinCount + " + checkinNum +" WHERE B.BusinessID='" + BusinessID + "'";
            SQLInsertStatements.add(sqlString);
        }
        catch(Exception e) {
            System.out.println("Error creating Checkins for " + BusinessID);
        }
    }


    public void executeInsertJDBC() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection connection = DriverManager.getConnection("jdbc:oracle:thin:@//127.0.0.1:1521/orcl", "scott", "oracle");
            Statement statement = connection.createStatement();
            for(String sqlString: SQLInsertStatements) {
                System.out.println("Attempting to Insert " + sqlString);
                Integer rowsInserted = statement.executeUpdate(sqlString);

                if (rowsInserted !=1) {
                    System.out.println("Insert didn't work " + sqlString);
                }
            }

            statement.close();
        }
        catch (Exception e) {
            System.out.println("something went wrong with insert statement");
        }

        SQLInsertStatements.clear();
    }

    public void executeDeleteJDBC() {

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection connection = DriverManager.getConnection("jdbc:oracle:thin:@//127.0.0.1:1521/orcl", "scott", "oracle");
            Statement statement = connection.createStatement();

            statement.executeUpdate("Delete From Business");
            statement.executeUpdate("Delete From YelpUser");
            statement.executeUpdate("Delete From Review");
            statement.executeUpdate("Delete From MainCategories");


            statement.close();

            System.out.println("Delete worked");
        }
        catch (Exception e) {
            System.out.println("something went wrong with delete statement " + e.getMessage());
        }
    }

    public void readFile(String filename) {

        Populate pop = new Populate();

        try {
            File file = new File(filename);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                JSONObject jobj = new JSONObject(line);
                pop.parseType(jobj);
            }
            fileReader.close();

        } catch (IOException e) {
            System.out.println("Couldn't open file: " + filename);
        }
    }

    public void parseType(JSONObject jsonObject) {

        try{

            String type = (String)jsonObject.get("type");

            //Parse specific type of objects
            switch (type) {
                case "business":
                    parseBusiness(jsonObject);
                    break;
                case "review":
                   parseReview(jsonObject);
                    break;
                case "user":
                    parseUser(jsonObject);
                    break;
                case "checkin": //not parsing checkins
                    parseCheckin(jsonObject);
                    break;
                case "tip": //not parsing tips
                    break;
            }

        } catch (Exception e) {
            String message = e.getMessage();
            System.out.println(message);
        };
    }

    public static void main(String[] args) {

        Populate pop = new Populate();

        //Delete old data
        pop.executeDeleteJDBC();

        //Open Files passed in as arguments
        String prefix = "/Users/julie/Documents/YelpDataset/";
        try {

            for (String fileName : args) {
                pop.readFile(prefix + fileName);
                pop.executeInsertJDBC();
            }
            pop.populateMainCategories();
        }
        catch (Exception e) {
            System.out.println("Couldn't open one of the files");
        }

    }
}


