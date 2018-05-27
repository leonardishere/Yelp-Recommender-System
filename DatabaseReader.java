package yelp_recommender_system;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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
	public static final String URL = "jdbc:mysql://localhost:9876/yelp_db?verifyServerCertificate=false&useSSL=true";
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
	
	/**
	 * Reads the entire attribute table from MySql then dumps it to Sqlite3 format.
	 */
	public static void dump_attribute() {
		System.out.println("Begin attribute table dump");
		Connection conn = null;
		BufferedWriter out = null;
		try{
			String filepath = "sql/attribute.sql";
			out = new BufferedWriter(new FileWriter(filepath));
			out.write("DROP TABLE IF EXISTS `attribute`;\n");
			out.write("CREATE TABLE `attribute` (\r\n" + 
					"  `id` int(11) NOT NULL,\r\n" + 
					"  `business_id` varchar(22) NOT NULL,\r\n" + 
					"  `name` varchar(255) DEFAULT NULL,\r\n" + 
					"  `value` mediumtext,\r\n" + 
					"  PRIMARY KEY (`id`)\r\n" + 
					");\n");
			
			
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			Statement stmt = conn.createStatement();
			String query = "select id, business_id, name, value from attribute";
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				out.write(String.format("insert into attribute values (%d, \"%s\", \"%s\", \"%s\");\n", rs.getInt(1), rs.getString(2), rs.getString(3).replaceAll("\"", "\"\""), rs.getString(4).replaceAll("\"", "\"\"")));
				if(rs.getInt(1) % 100000 == 0) out.write(String.format("select \"inserted %s row \" || id from %s where id=%d;\n", "attribute", "attribute", rs.getInt(1)));
			}
			out.write("select \"finished inserting into attribute. number of rows: \" || count(*) from attribute;\n");
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			try {
				if(out != null) out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				if(conn != null) conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("End attribute table dump");
	}
	
	/**
	 * Reads the entire business table from MySql then dumps it to Sqlite3 format.
	 */
	public static void dump_business() {
		System.out.println("Begin business table dump");
		Connection conn = null;
		BufferedWriter out = null;
		try{
			String filepath = "sql/business.sql";
			out = new BufferedWriter(new FileWriter(filepath));
			out.write("DROP TABLE IF EXISTS `business`;\n");
			out.write("CREATE TABLE `business` (\r\n" + 
					"  `id` varchar(22) NOT NULL,\r\n" + 
					"  `name` varchar(255) DEFAULT NULL,\r\n" + 
					"  `neighborhood` varchar(255) DEFAULT NULL,\r\n" + 
					"  `address` varchar(255) DEFAULT NULL,\r\n" + 
					"  `city` varchar(255) DEFAULT NULL,\r\n" + 
					"  `state` varchar(255) DEFAULT NULL,\r\n" + 
					"  `postal_code` varchar(255) DEFAULT NULL,\r\n" + 
					"  `latitude` float DEFAULT NULL,\r\n" + 
					"  `longitude` float DEFAULT NULL,\r\n" + 
					"  `stars` float DEFAULT NULL,\r\n" + 
					"  `review_count` int(11) DEFAULT NULL,\r\n" + 
					"  `is_open` tinyint(1) DEFAULT NULL,\r\n" + 
					"  PRIMARY KEY (`id`)\r\n" + 
					");\n");
			
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			Statement stmt = conn.createStatement();
			String query = "select id, name, neighborhood, address, city, state, postal_code, latitude, longitude, stars, review_count, is_open from business";
			ResultSet rs = stmt.executeQuery(query);
			int i = 0;
			while(rs.next()) {
				out.write(String.format("insert into business values (\"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", %f, %f, %f, %d, %d);\n", rs.getString(1), rs.getString(2).replaceAll("\"", "\"\""), rs.getString(3).replaceAll("\"", "\"\""), rs.getString(4).replaceAll("\"", "\"\""), rs.getString(5).replaceAll("\"", "\"\""), rs.getString(6).replaceAll("\"", "\"\""), rs.getString(7).replaceAll("\"", "\"\""), rs.getDouble(8), rs.getDouble(9), rs.getDouble(10), rs.getInt(11), rs.getInt(12)));
				++i;
				if(i % 100000 == 0) out.write(String.format("select \"inserted %s row %d. id = \" || id from %s where id = \"%s\";\n", "business", i, "business", rs.getString(1)));
			}
			out.write("select \"finished inserting into business. number of rows: \" || count(*) from business;\n");
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			try {
				if(out != null) out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				if(conn != null) conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("End business table dump");
	}

	/**
	 * Reads the entire category table from MySql then dumps it to Sqlite3 format.
	 */
	public static void dump_category() {
		System.out.println("Begin category table dump");
		Connection conn = null;
		BufferedWriter out = null;
		try{
			String filepath = "sql/category.sql";
			out = new BufferedWriter(new FileWriter(filepath));
			out.write("DROP TABLE IF EXISTS `category`;\n");
			out.write("CREATE TABLE `category` (\r\n" + 
					"  `id` int(11) NOT NULL,\r\n" + 
					"  `business_id` varchar(22) NOT NULL,\r\n" + 
					"  `category` varchar(255) DEFAULT NULL,\r\n" + 
					"  PRIMARY KEY (`id`)\r\n" + 
					");\n");
			
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			Statement stmt = conn.createStatement();
			String query = "select id, business_id, category from category";
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				out.write(String.format("insert into category values (%d, \"%s\", \"%s\");\n", rs.getInt(1), rs.getString(2), rs.getString(3).replaceAll("\"", "\"\"")));
				if(rs.getInt(1) % 100000 == 0) out.write(String.format("select \"inserted %s row \" || id from %s where id=%d;\n", "category", "category", rs.getInt(1)));
			}
			out.write("select \"finished inserting into category. number of rows: \" || count(*) from category;\n");
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			try {
				if(out != null) out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				if(conn != null) conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("End category table dump");
	}

	/**
	 * Reads the entire checkin table from MySql then dumps it to Sqlite3 format.
	 */
	public static void dump_checkin() {
		System.out.println("Begin checkin table dump");
		Connection conn = null;
		BufferedWriter out = null;
		try{
			String filepath = "sql/checkin.sql";
			out = new BufferedWriter(new FileWriter(filepath));
			out.write("DROP TABLE IF EXISTS `checkin`;\n");
			out.write("CREATE TABLE `checkin` (\r\n" + 
					"  `id` int(11) NOT NULL,\r\n" + 
					"  `business_id` varchar(22) NOT NULL,\r\n" + 
					"  `date` varchar(255) DEFAULT NULL,\r\n" + 
					"  `count` int(11) DEFAULT NULL,\r\n" + 
					"  PRIMARY KEY (`id`)\r\n" + 
					");\n");
			
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			Statement stmt = conn.createStatement();
			String query = "select id, business_id, date, count from checkin";
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				out.write(String.format("insert into checkin values (%d, \"%s\", \"%s\", %d);\n", rs.getInt(1), rs.getString(2), rs.getString(3).replaceAll("\"", "\"\""), rs.getInt(4)));
				if(rs.getInt(1) % 100000 == 0) out.write(String.format("select \"inserted %s row \" || id from %s where id=%d;\n", "checkin", "checkin", rs.getInt(1)));
			}
			out.write("select \"finished inserting into checkin. number of rows: \" || count(*) from checkin;\n");
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			try {
				if(out != null) out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				if(conn != null) conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("End checkin table dump");
	}

	/**
	 * Reads the entire elite_years table from MySql then dumps it to Sqlite3 format.
	 */
	public static void dump_elite_years() {
		System.out.println("Begin elite_years table dump");
		Connection conn = null;
		BufferedWriter out = null;
		try{
			String filepath = "sql/elite_years.sql";
			out = new BufferedWriter(new FileWriter(filepath));
			out.write("DROP TABLE IF EXISTS `elite_years`;\n");
			out.write("CREATE TABLE `elite_years` (\r\n" + 
					"  `id` int(11) NOT NULL,\r\n" + 
					"  `user_id` varchar(22) NOT NULL,\r\n" + 
					"  `year` char(4) NOT NULL,\r\n" + 
					"  PRIMARY KEY (`id`)\r\n" + 
					");\n");
			
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			Statement stmt = conn.createStatement();
			String query = "select id, user_id, year from elite_years";
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				out.write(String.format("insert into elite_years values (%d, \"%s\", \"%s\");\n", rs.getInt(1), rs.getString(2), rs.getString(3)));
				if(rs.getInt(1) % 100000 == 0) out.write(String.format("select \"inserted %s row \" || id from %s where id=%d;\n", "elite_years", "elite_years", rs.getInt(1)));
			}
			out.write("select \"finished inserting into elite_years. number of rows: \" || count(*) from elite_years;\n");
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			try {
				if(out != null) out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				if(conn != null) conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("End elite_years table dump");
	}

	/**
	 * Reads the entire friends table from MySql then dumps it to Sqlite3 format.
	 * Due to the size of this table, two output files are required.
	 */
	public static void dump_friends() {
		System.out.println("Begin friends table dump");
		Connection conn = null;
		BufferedWriter out1 = null, out2 = null;
		try{
			String filepath1 = "sql/friends_1.sql";
			out1 = new BufferedWriter(new FileWriter(filepath1));
			out1.write("DROP TABLE IF EXISTS `friends`;\n");
			out1.write("CREATE TABLE `friend` (\r\n" + 
					"  `id` int(11) NOT NULL,\r\n" + 
					"  `user_id` varchar(22) NOT NULL,\r\n" + 
					"  `friend_id` varchar(22) NOT NULL,\r\n" + 
					"  PRIMARY KEY (`id`)\r\n" + 
					");\n");
			
			for(int i = 0; i < 25000000; i += 1000000){
				Class.forName("com.mysql.cj.jdbc.Driver");
				conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
				Statement stmt = conn.createStatement();
				String query = "select id, user_id, friend_id from friend limit " + i + ", 1000000";
				ResultSet rs = stmt.executeQuery(query);
				while(rs.next()) {
					out1.write(String.format("insert into friends values (%d, \"%s\", \"%s\");\n", rs.getInt(1), rs.getString(2), rs.getString(3)));
					if(rs.getInt(1) % 100000 == 0) out1.write(String.format("select \"inserted %s row \" || id from %s where id=%d;\n", "friends", "friends", rs.getInt(1)));
				}
				conn.close();
				System.out.printf("%10d friends\n", i);
			}
			
			String filepath2 = "sql/friends_2.sql";
			out2 = new BufferedWriter(new FileWriter(filepath2));
			for(int i = 25000000; i < 49626957; i += 1000000){
				Class.forName("com.mysql.cj.jdbc.Driver");
				conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
				Statement stmt = conn.createStatement();
				String query = "select id, user_id, friend_id from friend limit " + i + ", 1000000";
				ResultSet rs = stmt.executeQuery(query);
				while(rs.next()) {
					out2.write(String.format("insert into friends values (%d, \"%s\", \"%s\");\n", rs.getInt(1), rs.getString(2), rs.getString(3)));
					if(rs.getInt(1) % 100000 == 0) out2.write(String.format("select \"inserted %s row \" || id from %s where id=%d;\n", "friends", "friends", rs.getInt(1)));
				}
				conn.close();
				System.out.printf("%10d friends\n", i);
			}
			out2.write("select \"finished inserting into friends. number of rows: \" || count(*) from friends;");
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			try {
				if(out1 != null) out1.close();
				if(out2 != null) out2.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				if(conn != null) conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("End friends table dump");
	}

	/**
	 * Reads the entire hours table from MySql then dumps it to Sqlite3 format.
	 */
	public static void dump_hours() {
		System.out.println("Begin hours table dump");
		Connection conn = null;
		BufferedWriter out = null;
		try{
			String filepath = "sql/hours.sql";
			out = new BufferedWriter(new FileWriter(filepath));
			out.write("DROP TABLE IF EXISTS `hours`;\n");
			out.write("CREATE TABLE `hours` (\r\n" + 
					"  `id` int(11) NOT NULL,\r\n" + 
					"  `business_id` varchar(22) NOT NULL,\r\n" + 
					"  `hours` varchar(255) DEFAULT NULL,\r\n" + 
					"  PRIMARY KEY (`id`)\r\n" + 
					") ;\n");
			
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			Statement stmt = conn.createStatement();
			String query = "select id, business_id, hours from hours";
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				out.write(String.format("insert into hours values (%d, \"%s\", \"%s\");\n", rs.getInt(1), rs.getString(2), rs.getString(3)));
				if(rs.getInt(1) % 100000 == 0) out.write(String.format("select \"inserted %s row \" || id from %s where id=%d;\n", "hours", "hours", rs.getInt(1)));
			}
			out.write("select \"finished inserting into hours. number of rows: \" || count(*) from hours;\n");
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			try {
				if(out != null) out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				if(conn != null) conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("End hours table dump");
	}

	/**
	 * Reads the entire photo table from MySql then dumps it to Sqlite3 format.
	 */
	public static void dump_photo() {
		System.out.println("Begin photo table dump");
		Connection conn = null;
		BufferedWriter out = null;
		try{
			String filepath = "sql/photo.sql";
			out = new BufferedWriter(new FileWriter(filepath));
			out.write("DROP TABLE IF EXISTS `photo`;\n");
			out.write("CREATE TABLE `photo` (\r\n" + 
					"  `id` varchar(22) NOT NULL,\r\n" + 
					"  `business_id` varchar(22) NOT NULL,\r\n" + 
					"  `caption` varchar(255) DEFAULT NULL,\r\n" + 
					"  `label` varchar(255) DEFAULT NULL,\r\n" + 
					"  PRIMARY KEY (`id`)\r\n" + 
					");\n");
			
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			Statement stmt = conn.createStatement();
			String query = "select id, business_id, caption, label from photo";
			ResultSet rs = stmt.executeQuery(query);
			int i = 0;
			while(rs.next()) {
				out.write(String.format("insert into photo values (\"%s\", \"%s\", \"%s\", \"%s\");\n", rs.getString(1), rs.getString(2), rs.getString(3).replaceAll("\"", "\"\""), rs.getString(4).replaceAll("\"", "\"\"")));
				++i;
				if(i % 100000 == 0) out.write(String.format("select \"inserted %s row %d. id = \" || id from %s where id = \"%s\";\n", "photo", i, "photo", rs.getString(1)));
			}
			out.write("select \"finished inserting into photo. number of rows: \" || count(*) from photo;\n");
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			try {
				if(out != null) out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				if(conn != null) conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("End photo table dump");
	}

	/**
	 * Reads the entire review table from MySql then dumps it to Sqlite3 format.
	 */
	public static void dump_review() {
		System.out.println("Begin review table dump");
		Connection conn = null;
		BufferedWriter out1 = null, out2 = null;
		try{
			String filepath1 = "sql/review_1.sql";
			out1 = new BufferedWriter(new FileWriter(filepath1));
			out1.write("DROP TABLE IF EXISTS `review`;\n");
			out1.write("CREATE TABLE `review` (\r\n" + 
					"  `id` varchar(22) NOT NULL,\r\n" + 
					"  `business_id` varchar(22) NOT NULL,\r\n" + 
					"  `user_id` varchar(22) NOT NULL,\r\n" + 
					"  `stars` int(11) DEFAULT NULL,\r\n" + 
					"  `date` datetime DEFAULT NULL,\r\n" + 
					"  `text` mediumtext,\r\n" + 
					"  `useful` int(11) DEFAULT NULL,\r\n" + 
					"  `funny` int(11) DEFAULT NULL,\r\n" + 
					"  `cool` int(11) DEFAULT NULL,\r\n" + 
					"  PRIMARY KEY (`id`)\r\n" + 
					") ;\n");

			int j = 0;
			for(int i = 0; i < 2500000; i += 1000000){
				Class.forName("com.mysql.cj.jdbc.Driver");
				conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
				Statement stmt = conn.createStatement();
				String query = "select id, business_id, user_id, stars, date, text, useful, funny, cool from review limit " + i + ", 1000000";
				ResultSet rs = stmt.executeQuery(query);
				while(rs.next()) {
					out1.write(String.format("insert into review values (\"%s\", \"%s\", \"%s\", %d, \"%s\", \"%s\", %d, %d, %d);\n", rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getString(5), rs.getString(6).replaceAll("\"", "\"\""), rs.getInt(7), rs.getInt(8), rs.getInt(9)));
					++j;
					if(j % 100000 == 0) out1.write(String.format("select \"inserted %s row %d. id = \" || id from %s where id = \"%s\";\n", "review", j, "review", rs.getString(1)));
				}
				conn.close();
				System.out.printf("%10d reviews\n", i);
			}

			String filepath2 = "sql/review_2.sql";
			out2 = new BufferedWriter(new FileWriter(filepath2));
			for(int i = 2500000; i < 5261669; i += 1000000){
				Class.forName("com.mysql.cj.jdbc.Driver");
				conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
				Statement stmt = conn.createStatement();
				String query = "select id, business_id, user_id, stars, date, text, useful, funny, cool from review limit " + i + ", 1000000";
				ResultSet rs = stmt.executeQuery(query);
				while(rs.next()) {
					out2.write(String.format("insert into review values (\"%s\", \"%s\", \"%s\", %d, \"%s\", \"%s\", %d, %d, %d);\n", rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getString(5), rs.getString(6).replaceAll("\"", "\"\""), rs.getInt(7), rs.getInt(8), rs.getInt(9)));
					++j;
					if(j % 100000 == 0) out2.write(String.format("select \"inserted %s row %d \" || id from %s where strcmp(id,\"%s\")=0;\n", "review", j, "review", rs.getString(1)));
				}
				conn.close();
				System.out.printf("%10d reviews\n", i);
			}
			out1.write("select \"finished inserting into review. number of rows: \" || count(*) from review;\n");
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			try {
				if(out1 != null) out1.close();
				if(out2 != null) out2.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				if(conn != null) conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("End review table dump");
	}

	/**
	 * Reads the entire tip table from MySql then dumps it to Sqlite3 format.
	 */
	public static void dump_tip() {
		System.out.println("Begin tip table dump");
		Connection conn = null;
		BufferedWriter out = null;
		try{
			String filepath = "sql/tip.sql";
			out = new BufferedWriter(new FileWriter(filepath));
			out.write("DROP TABLE IF EXISTS `tip`;\n");
			out.write("CREATE TABLE `tip` (\r\n" + 
					"  `id` int(11) NOT NULL,\r\n" + 
					"  `user_id` varchar(22) NOT NULL,\r\n" + 
					"  `business_id` varchar(22) NOT NULL,\r\n" + 
					"  `text` mediumtext, \r\n" + 
					"  `date` datetime DEFAULT NULL,\r\n" + 
					"  `likes` int(11) DEFAULT NULL,\r\n" + 
					"  PRIMARY KEY (`id`)\r\n" + 
					");\n");
			
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			Statement stmt = conn.createStatement();
			String query = "select id, user_id, business_id, text, date, likes from tip";
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				out.write(String.format("insert into tip values (%d, \"%s\", \"%s\", \"%s\", \"%s\", %d);\n", rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4).replaceAll("\"", "\"\""), rs.getString(5), rs.getInt(6)));
				if(rs.getInt(1) % 100000 == 0) out.write(String.format("select \"inserted %s row \" || id from %s where id=%d;\n", "tip", "tip", rs.getInt(1)));
			}
			out.write("select \"finished inserting into tip. number of rows: \" || count(*) from tip;\n");
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			try {
				if(out != null) out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				if(conn != null) conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("End tip table dump");
	}

	/**
	 * Reads the entire user table from MySql then dumps it to Sqlite3 format.
	 */
	public static void dump_user() {
		System.out.println("Begin user table dump");
		Connection conn = null;
		BufferedWriter out = null;
		try{
			String filepath = "sql/user.sql";
			out = new BufferedWriter(new FileWriter(filepath));
			out.write("DROP TABLE IF EXISTS `user`;\n");
			out.write("CREATE TABLE `user` (\r\n" + 
					"  `id` varchar(22) NOT NULL,\r\n" + 
					"  `name` varchar(255) DEFAULT NULL,\r\n" + 
					"  `review_count` int(11) DEFAULT NULL,\r\n" + 
					"  `yelping_since` datetime DEFAULT NULL,\r\n" + 
					"  `useful` int(11) DEFAULT NULL,\r\n" + 
					"  `funny` int(11) DEFAULT NULL,\r\n" + 
					"  `cool` int(11) DEFAULT NULL,\r\n" + 
					"  `fans` int(11) DEFAULT NULL,\r\n" + 
					"  `average_stars` float DEFAULT NULL,\r\n" + 
					"  `compliment_hot` int(11) DEFAULT NULL,\r\n" + 
					"  `compliment_more` int(11) DEFAULT NULL,\r\n" + 
					"  `compliment_profile` int(11) DEFAULT NULL,\r\n" + 
					"  `compliment_cute` int(11) DEFAULT NULL,\r\n" + 
					"  `compliment_list` int(11) DEFAULT NULL,\r\n" + 
					"  `compliment_note` int(11) DEFAULT NULL,\r\n" + 
					"  `compliment_plain` int(11) DEFAULT NULL,\r\n" + 
					"  `compliment_cool` int(11) DEFAULT NULL,\r\n" + 
					"  `compliment_funny` int(11) DEFAULT NULL,\r\n" + 
					"  `compliment_writer` int(11) DEFAULT NULL,\r\n" + 
					"  `compliment_photos` int(11) DEFAULT NULL,\r\n" + 
					"  PRIMARY KEY (`id`)\r\n" + 
					");\n");
			
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			Statement stmt = conn.createStatement();
			String query = "select id, name, review_count, yelping_since, useful, funny, cool, fans, average_stars, compliment_hot, compliment_more, compliment_profile, compliment_cute, compliment_list, compliment_note, compliment_plain, compliment_cool, compliment_funny, compliment_writer, compliment_photos from user";
			ResultSet rs = stmt.executeQuery(query);
			int i = 0;
			while(rs.next()) {
				out.write(String.format("insert into user values (\"%s\", \"%s\", %d, \"%s\", %d, %d, %d, %d, %f, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d);\n", rs.getString(1), rs.getString(2), rs.getInt(3), rs.getString(4), rs.getInt(5), rs.getInt(6), rs.getInt(7), rs.getInt(8), rs.getDouble(9), rs.getInt(10), rs.getInt(11), rs.getInt(12), rs.getInt(13), rs.getInt(14), rs.getInt(15), rs.getInt(16), rs.getInt(17), rs.getInt(18), rs.getInt(19), rs.getInt(20)));
				++i;
				if(i % 100000 == 0) out.write(String.format("select \"inserted %s row %d. id = \" || id from %s where id = \"%s\";\n", "user", i, "user", rs.getString(1)));
			}
			out.write("select \"finished inserting into user. number of rows: \" || count(*) from user;\n");
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			try {
				if(out != null) out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				if(conn != null) conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("End user table dump");
	}

	/**
	 * Reads the entire database from MySql then dumps it to Sqlite3 format.
	 * Before running this, it may be neccessary to create a folder called sql two levels up.
	 */
	public static void dump_all() {
		dump_attribute();
		dump_business();
		dump_category();
		dump_checkin();
		dump_elite_years();
		//dump_friends();
		dump_hours();
		dump_photo();
		//dump_review();
		dump_tip();
		dump_user();
	}
	
	public static void stringReplaceTest() {
		System.out.println("test \" string".replaceAll("\"", "\"\""));
	}
	
	/**
	 * Main method.
	 * @param args ignored
	 */
	public static void main(String[] args) {
		dump_all();
		//dump_photo();
		//dump_friends();
		//dump_review();
		//stringReplaceTest();
	}
}
