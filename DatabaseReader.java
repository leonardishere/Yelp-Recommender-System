import java.sql.Connection;
import java.sql.DriverManager;
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
	public static final String URL = "jdbc:mysql://localhost:9876/yelp_db?verifyServerCertificate=false&useSSL=true";
	public static final String USERNAME = "root";
	public static final String PASSWORD = "root";
	
	/**
	 * Loads the users from database into memory.
	 * @return the user
	 */
	public static User[] loadUsers() {
		//KeyMap userMap = new KeyMap();
		ArrayList<User> users = new ArrayList<>();
		Connection conn = null;
		try{
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			Statement stmt = conn.createStatement();
			String query = "select distinct id from user order by id asc";
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				//String id = rs.getString(1);
				//userMap.add(id);
				users.add(new User(rs.getString(1)));
			}
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			try {
				if(conn != null) conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		User[] users2 = new User[users.size()];
		users.toArray(users2);
		return users2;
	}
	
	/**
	 * Loads the users from database into memory.
	 * @return the business 
	 */
	public static Business[] loadBusinesses() {
		//KeyMap businessMap = new KeyMap();
		ArrayList<Business> businesses = new ArrayList<>();
		Connection conn = null;
		try{
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			Statement stmt = conn.createStatement();
			String query = "select id, name, neighborhood, address, city, state, postal_code, latitude, longitude, stars, review_count, is_open from business order by id asc";
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				//String id = rs.getString(1);
				//businessMap.add(id);
				businesses.add(new Business(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), rs.getDouble(8), rs.getDouble(9), rs.getDouble(10), rs.getInt(11), rs.getBoolean(12)));
			}
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			try {
				if(conn != null) conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		Business[] businesses2 = new Business[businesses.size()];
		businesses.toArray(businesses2);
		return businesses2;
	}
	
	public static ArrayList<UserBusinessInteraction> loadTrainingData(){
		ArrayList<UserBusinessInteraction> interactions = new ArrayList<>();
		Connection conn = null;
		try{
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			Statement stmt = conn.createStatement();
			String query = "select user_id, business_id, stars from review where date < datetime('2017-01-27')"; //check date
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				interactions.add(new UserBusinessInteraction(rs.getString(1), rs.getString(2), rs.getInt(3)));
			}
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			try {
				if(conn != null) conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return interactions;
	}
	
	public static ArrayList<UserBusinessInteraction> loadTestingData(){
		ArrayList<UserBusinessInteraction> interactions = new ArrayList<>();
		Connection conn = null;
		try{
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			Statement stmt = conn.createStatement();
			String query = "select user_id, business_id, stars from review where date >= datetime('2017-01-27')"; //check date
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				interactions.add(new UserBusinessInteraction(rs.getString(1), rs.getString(2), rs.getInt(3)));
			}
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			try {
				if(conn != null) conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return interactions;
	}
	
	/**
	 * Loads the reviews from database into the actual rating matrix.
	 * This matrix would be multiple terrabytes and thus is not the way to go.
	 * @param userMap the map of userIDs to indices
	 * @param businessMap the map of businessIDs to indices
	 * @return the actual rating matrix
	 */
	public static double[][][] loadActualRatingMatrix(KeyMap userMap, KeyMap businessMap) {
		int numStars = 5;
		int numUsers = userMap.size();
		int numBusinesses = businessMap.size();
		long numBytes = ((long)numStars*numUsers)*((long)numBusinesses*8);
		System.out.printf("Constructing a %,d * %,d * %,d matrix (%,d bytes)\n", numStars, numUsers, numBusinesses, numBytes);
		double[][][] actualRatingMatrix = Helper.zeros(numStars, numUsers, numBusinesses);
		Connection conn = null;
		try{
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			Statement stmt = conn.createStatement();
			String query = "select stars, user.id as user_id, business.id as business_id from users inner join review on user.id=review.user_id inner join business on review.business_id=business.id";
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				int stars = rs.getInt(1);
				if(rs.wasNull()) continue;
				String userId = rs.getString(2);
				int userIndex = userMap.convert(userId);
				String businessId = rs.getString(3);
				int businessIndex = businessMap.convert(businessId);
				actualRatingMatrix[stars-1][userIndex][businessIndex] = 1.0;
			}
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			try {
				if(conn != null) conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return actualRatingMatrix;
	}
}
