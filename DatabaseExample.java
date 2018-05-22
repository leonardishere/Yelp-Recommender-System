package yelp_recommender_system;

import java.sql.*; 

//note: installation of an external database driver is required.
//in my current setup with a mysql server running on localhost,
//guide to installing driver: https://stackoverflow.com/questions/2839321/connect-java-to-a-mysql-database
//link to current driver: https://dev.mysql.com/downloads/connector/j/

/**
 * This class provides an example of how to read a database.
 * @author Andrew Leonard
 *
 */
public class DatabaseExample {
	public static final String URL = "jdbc:mysql://localhost:3306/yelp_db?verifyServerCertificate=false&useSSL=true";
	public static final String USERNAME = "root";
	public static final String PASSWORD = "root";
	
	public static void main(String args[]){
		Connection con = null;
		try{
			Class.forName("com.mysql.cj.jdbc.Driver");
			con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			Statement stmt = con.createStatement();
			String query = "select id, name, review_count from user limit 10";
			ResultSet rs = stmt.executeQuery(query);
			String line = dividerLine(new int[]{22,15,11});
			System.out.println(line);
			System.out.printf("| %-22s | %-15s | %-11s |\n", "id", "name", "reviews");
			System.out.println(line);
			while(rs.next()) {
				//index 0 is invalid
				System.out.printf("| %-22s | %-15s | %11d |\n", rs.getString(1), rs.getString(2), rs.getInt(3));
			}
			System.out.println(line);
			//con.close();
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			try {
				if(con != null) con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Creates a divider line to be printed with a table.
	 * @param fieldWidths the width of each field in order
	 * @return a string divide
	 */
	public static String dividerLine(int[] fieldWidths) {
		StringBuilder builder = new StringBuilder("+");
		for(int width : fieldWidths) {
			for(int i = 0; i < width+2; ++i) {
				builder.append("-");
			}
			builder.append("+");
		}
		return builder.toString();
	}
}
