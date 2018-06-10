import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * This class provides a high level interface to interacting with the database.
 * @author Team 2
 *
 */
public class DatabaseReader {
	public static final String MYSQL_URL = "jdbc:mysql://localhost:9876/yelp_db?verifyServerCertificate=false&useSSL=true";
	public static final String USERNAME = "root";
	public static final String PASSWORD = "root";

	public static final String SQLITE_URL = "jdbc:sqlite:/local/weka/yelp.db";
	public static final String SQLITE_RESTAURANT_URL = "jdbc:sqlite:/local/weka/yelp_restaurants.db";

	public static final int NUM_USERS = 					1326101;
	public static final int NUM_BUSINESSES = 				 174567;
	public static final int NUM_ATTRIBUTES =				1310575;
	public static final int NUM_CATEGORIES = 				 667527;
	public static final int NUM_TRAINING_DATA = 			4208386;
	public static final int NUM_TESTING_DATA = 				1053283;

	public static final int NUM_USERS_RESTAURANTS = 		 908921;
	public static final int NUM_RESTAURANTS = 	 			  54618;
	public static final int NUM_RESTAURANT_ATTRIBUTES = 	 904202;
	public static final int NUM_RESTAURANT_CATEGORIES = 	 210956;
	public static final int NUM_TRAINING_RESTAURANTS = 		2597861;
	public static final int NUM_TESTING_RESTAURANTS = 		 623558;

	/**
	 * Creates a connection to the mysql database.
	 * @return
	 */
	public static Connection connect_mysql() {
		Connection conn = null;
		try{
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(MYSQL_URL, USERNAME, PASSWORD);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return conn;
	}

	/**
	 * Creates a connection to the sqlite database.
	 * @return
	 */
	public static Connection connect_sqlite(boolean restaurantsOnly) {
		Connection conn = null;
		try{
			conn = restaurantsOnly
					? DriverManager.getConnection(SQLITE_RESTAURANT_URL)
					: DriverManager.getConnection(SQLITE_URL);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return conn;
	}

	/**
	 * Loads the users from database into memory.
	 * @return the user
	 */
	public static User[] loadUsers(Connection conn, boolean restaurantsOnly) {
		ArrayList<User> users = new ArrayList<>(restaurantsOnly ? NUM_USERS_RESTAURANTS : NUM_USERS);

		Statement stmt;
		try {
			stmt = conn.createStatement();
			String query = "select distinct id from user order by id asc";
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				users.add(new User(rs.getString(1)));
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		User[] users2 = new User[users.size()];
		users.toArray(users2);
		return users2;
	}

	/**
	 * Loads the users from database into memory.
	 * @return the business 
	 */
	public static Business[] loadBusinesses(Connection conn, boolean restaurantsOnly) {
		ArrayList<Business> businesses = new ArrayList<>(restaurantsOnly ? NUM_RESTAURANTS : NUM_BUSINESSES);
		try{
			Statement stmt = conn.createStatement();
			String query = "select id, name, neighborhood, address, city, state, postal_code, latitude, longitude, stars, review_count, is_open from business order by id asc";
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				businesses.add(new Business(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), rs.getDouble(8), rs.getDouble(9), rs.getDouble(10), rs.getInt(11), rs.getBoolean(12)));
			}
			rs.close();
		} catch(Exception e){
			e.printStackTrace();
		} 

		Business[] businesses2 = new Business[businesses.size()];
		businesses.toArray(businesses2);
		return businesses2;
	}

	public static ArrayList<BusinessAttribute> loadAttributes(Connection conn, boolean restaurantsOnly){
		ArrayList<BusinessAttribute> attributes = new ArrayList<>(restaurantsOnly ? NUM_RESTAURANT_ATTRIBUTES : NUM_ATTRIBUTES);
		try{
			Statement stmt = conn.createStatement();
			String query = "select business_id, name, value from attribute";
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				attributes.add(new BusinessAttribute(rs.getString(1), rs.getString(2), rs.getString(3)));
			}
			rs.close();
		} catch(Exception e){
			e.printStackTrace();
		}
		return attributes;
	}

	public static ArrayList<BusinessAttribute> loadCategories(Connection conn, boolean restaurantsOnly){
		ArrayList<BusinessAttribute> categories = new ArrayList<>(restaurantsOnly ? NUM_RESTAURANT_CATEGORIES : NUM_CATEGORIES);
		try{
			Statement stmt = conn.createStatement();
			String query = "select business_id, category from category";
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				categories.add(new BusinessAttribute(rs.getString(1), "Category", rs.getString(2)));
			}
			rs.close();
		} catch(Exception e){
			e.printStackTrace();
		}
		return categories;
	}

	public static ArrayList<UserBusinessInteraction> loadTrainingData(Connection conn, boolean useSqlite, boolean restaurantsOnly){
		ArrayList<UserBusinessInteraction> interactions = new ArrayList<>(restaurantsOnly ? NUM_TRAINING_RESTAURANTS : NUM_TRAINING_DATA);
		try{
			int step = 1000000;
			for(int i = 0; i < NUM_TESTING_DATA; i += step) {
				Statement stmt = conn.createStatement();
				String query = useSqlite 
						? "select user_id, business_id, stars from review where date < datetime('2017-01-27') limit " + i + ", " + step 
								: "select user_id, business_id, stars from review where date < '2017-01-27' limit " + i + ", " + step;
				ResultSet rs = stmt.executeQuery(query);
				while(rs.next()) {
					interactions.add(new UserBusinessInteraction(rs.getString(1), rs.getString(2), rs.getInt(3)));
				}
				rs.close();
			}
		} catch(Exception e){
			e.printStackTrace();
		}
		return interactions;
	}

	public static ArrayList<UserBusinessInteraction> loadTestingData(Connection conn, boolean useSqlite, boolean restaurantsOnly){
		ArrayList<UserBusinessInteraction> interactions = new ArrayList<>(restaurantsOnly ? NUM_TESTING_RESTAURANTS : NUM_TESTING_DATA);
		try{
			Statement stmt = conn.createStatement();
			String query = useSqlite ? "select user_id, business_id, stars from review where date >= datetime('2017-01-27')" : "select user_id, business_id, stars from review where date >= '2017-01-27'"; 
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				interactions.add(new UserBusinessInteraction(rs.getString(1), rs.getString(2), rs.getInt(3)));
			}
			rs.close();
		} catch(Exception e){
			e.printStackTrace();
		}
		return interactions;
	}
	
	public static ArrayList<UserBusinessInteraction> loadReviewsForUser(Connection conn, String userID){
		ArrayList<UserBusinessInteraction> interactions = new ArrayList<>();
		try{
			Statement stmt = conn.createStatement();
			String query = "select business_id, stars from review where user_id=\""+userID+"\" order by date asc";
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				interactions.add(new UserBusinessInteraction(userID, rs.getString(1), rs.getInt(2)));
			}
			rs.close();
		} catch(Exception e){
			e.printStackTrace();
		}
		return interactions;
	}

	public static String getLatestModel(Connection conn) {
		try {
			Statement stmt = conn.createStatement();
			String query = "select iters, filepath from model where iters in (select max(iters) from model);";
			ResultSet rs = stmt.executeQuery(query);
			if(rs.next()) {
				return rs.getString(2);
			}
			rs.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean saveModel(Connection conn, int numIters, String filepath) {
		try {
			//conn.setAutoCommit(false);
			String sqlStatement = "INSERT INTO model(iters, filepath) VALUES(?,?);";
			PreparedStatement preparedStatement = conn.prepareStatement(sqlStatement);
			preparedStatement.setInt(1, numIters);
			preparedStatement.setString(2, filepath);
			//preparedStatement.addBatch();
			//preparedStatement.executeBatch();            
			preparedStatement.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean saveModel(Connection conn, int numIters, String filepath, double rmse) {
		try {
			//conn.setAutoCommit(false);
			String sqlStatement = "INSERT INTO model(iters, filepath, rmse) VALUES(?,?,?);";
			PreparedStatement preparedStatement = conn.prepareStatement(sqlStatement);
			preparedStatement.setInt(1, numIters);
			preparedStatement.setString(2, filepath);
			preparedStatement.setDouble(3, rmse);
			//preparedStatement.addBatch();
			//preparedStatement.executeBatch();            
			preparedStatement.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
