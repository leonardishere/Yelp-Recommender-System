import java.sql.*; 

//note: installation of an external database driver is required.
//in my current setup with a mysql server running on localhost,
//guide to installing driver: https://stackoverflow.com/questions/2839321/connect-java-to-a-mysql-database
//link to current driver: https://dev.mysql.com/downloads/connector/j/

/**
 * This class provides an example of how to read a database.
 * @author Team 2
 *
 */
public class DatabaseExample {
	public static final String URL = "jdbc:mysql://localhost:9876/yelp_db?verifyServerCertificate=false&useSSL=true";
	public static final String USERNAME = "root";
	public static final String PASSWORD = "root";

	public static void mysqlExample(){
		Connection con = null;
		try{
			Class.forName("com.mysql.cj.jdbc.Driver");
			con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			Statement stmt = con.createStatement();
			String query = "select id, name, review_count from user limit 10";
			ResultSet rs = stmt.executeQuery(query);
			String line = Helper.dividerLine(new int[]{22,15,11});
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

	public static void sqliteExample() {
		Connection conn = null;
		try {
			// db parameters
			String url = "jdbc:sqlite:C:/sqlite/db/yelp.db";
			// create a connection to the database
			conn = DriverManager.getConnection(url);

			System.out.println("Connection to SQLite has been established.");

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException ex) {
				System.out.println(ex.getMessage());
			}
		}
	}

	public static void main(String[] args) {
		//mysqlExample();
		sqliteExample();
	}
}
