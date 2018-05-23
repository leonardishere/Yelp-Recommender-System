package yelp_recommender_system;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class provides a high level interface to interacting with the database.
 * @author Team 2
 *
 */
public class DatabaseReader {
	public static final String URL = "jdbc:mysql://localhost:3306/yelp_db?verifyServerCertificate=false&useSSL=true";
	public static final String USERNAME = "root";
	public static final String PASSWORD = "root";
	
	/**
	 * Loads the users from database into a KeyMap.
	 * @return the user map
	 */
	public static KeyMap loadUsers() {
		KeyMap userMap = new KeyMap();
		Connection conn = null;
		try{
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			Statement stmt = conn.createStatement();
			String query = "select distinct id from user order by id asc";
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				String id = rs.getString(1);
				userMap.add(id);
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
		return userMap;
	}
	
	/**
	 * Loads the users from database into a KeyMap.
	 * @return the business map
	 */
	public static KeyMap loadBusinesses() {
		KeyMap businessMap = new KeyMap();
		Connection conn = null;
		try{
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			Statement stmt = conn.createStatement();
			String query = "select distinct id from business order by id asc";
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				String id = rs.getString(1);
				businessMap.add(id);
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
		return businessMap;
	}
	
	/**
	 * Loads the reviews from database into the actual rating matrix.
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
