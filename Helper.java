package yelp_recommender_system;

/**
 * Helper provides some static helper methods that have no home elsewhere.
 * @author Team 2
 *
 */
public class Helper {
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

	/**
	 * Creates a 2D matrix with all elements initialized to zero.
	 * @param numRows the number of rows in the matrix (dimension 1)
	 * @param numCols the number of cols in the matrix (dimension 2)
	 * @return the matrix
	 */
	public static double[][] zeros(int numRows, int numCols){
		double[][] matrix = new double[numRows][numCols];
		for(int row = 0; row < numRows; ++row) {
			for(int col = 0; col < numCols; ++col) {
				matrix[row][col] = 0;
			}
		}
		return matrix;
	}
	
	/**
	 * Creates a 3D matrix with all elements initialized to zero.
	 * @param numPages the number of pages in the matrix (dimension 1)
	 * @param numRows the number of rows in the matrix (dimension 2)
	 * @param numCols the number of cols in the matrix (dimension 3)
	 * @return the matrix
	 */
	public static double[][][] zeros(int numPages, int numRows, int numCols){
		double[][][] matrix = new double[numPages][numRows][numCols];
		for(int page = 0; page < numPages; ++page) {
			for(int row = 0; row < numRows; ++row) {
				for(int col = 0; col < numCols; ++col) {
					matrix[page][row][col] = 0;
				}
			}
		}
		return matrix;
	}
	
	/**
	 * Converts the 2D matrix to CSV.
	 * @param matrix the matrix to convert
	 * @param formatter the format of each value. use "%f" as a default
	 * @return a string representation of the CSV
	 */
	public static String toCSV(double[][] matrix, String formatter) {
		StringBuilder builder = new StringBuilder();
		for(int row = 0; row < matrix.length; ++row) {
			for(int col = 0; col < matrix[row].length; ++col) {
				builder.append(String.format(formatter, matrix[row][col]));
				builder.append(",");
			}
			builder.append("\n");
		}
		return builder.toString();
	}
}
