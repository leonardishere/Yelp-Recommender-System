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

	public static final String SQLITE_URL = "jdbc:sqlite:C:/sqlite/db/yelp.db";
	
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
	public static Connection connect_sqlite() {
		Connection conn = null;
		try{
		    conn = DriverManager.getConnection(SQLITE_URL);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return conn;
	}
	
	/**
	 * Loads the users from database into memory.
	 * @return the user
	 */
	public static User[] loadUsers(Connection conn) {
		ArrayList<User> users = new ArrayList<>();
		
		Statement stmt;
		try {
			stmt = conn.createStatement();
			String query = "select distinct id from user order by id asc";
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				//String id = rs.getString(1);
				//userMap.add(id);
				users.add(new User(rs.getString(1)));
			}			
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
	public static Business[] loadBusinesses(Connection conn) {
		ArrayList<Business> businesses = new ArrayList<>();
		try{
			Statement stmt = conn.createStatement();
			String query = "select id, name, neighborhood, address, city, state, postal_code, latitude, longitude, stars, review_count, is_open from business order by id asc";
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				businesses.add(new Business(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), rs.getDouble(8), rs.getDouble(9), rs.getDouble(10), rs.getInt(11), rs.getBoolean(12)));
			}
		} catch(Exception e){
			e.printStackTrace();
		} 
		
		Business[] businesses2 = new Business[businesses.size()];
		businesses.toArray(businesses2);
		return businesses2;
	}
	
	public static ArrayList<UserBusinessInteraction> loadTrainingData(Connection conn, boolean useSqlite){
		ArrayList<UserBusinessInteraction> interactions = new ArrayList<>();
		try{
			Statement stmt = conn.createStatement();
			String query = useSqlite ? "select user_id, business_id, stars from review where date < datetime('2017-01-27')" : "select user_id, business_id, stars from review where date < '2017-01-27'";
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				interactions.add(new UserBusinessInteraction(rs.getString(1), rs.getString(2), rs.getInt(3)));
			}
		} catch(Exception e){
			e.printStackTrace();
		}
		return interactions;
	}
	
	public static ArrayList<UserBusinessInteraction> loadTestingData(Connection conn, boolean useSqlite){
		ArrayList<UserBusinessInteraction> interactions = new ArrayList<>();
		try{
			Statement stmt = conn.createStatement();
			String query = useSqlite ? "select user_id, business_id, stars from review where date >= datetime('2017-01-27')" : "select user_id, business_id, stars from review where date >= '2017-01-27'"; 
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				interactions.add(new UserBusinessInteraction(rs.getString(1), rs.getString(2), rs.getInt(3)));
			}
		} catch(Exception e){
			e.printStackTrace();
		}
		return interactions;
	}

	public static String getLatestModel(Connection conn) {
		try {
			Statement stmt = conn.createStatement();
	        String query = "select iters, filepath from models where iters in (select max(iters) from models);";
	        ResultSet rs = stmt.executeQuery(query);
	        if(rs.next()) {
	        	return rs.getString(2);
	        }
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static boolean saveModel(Connection conn, int numIters, String filepath) {
		try {
            conn.setAutoCommit(false);
            String sqlStatement = "INSERT INTO models(iter, filepath) VALUES(?,?);";
        	PreparedStatement preparedStatement = conn.prepareStatement(sqlStatement);
            preparedStatement.setInt(1, numIters);
            preparedStatement.setString(2, filepath);
            preparedStatement.addBatch();
            preparedStatement.executeBatch();            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
		return true;
	}
}
