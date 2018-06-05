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
	 * Creates an array with all elements initialized to zero.
	 * @param length the number of elements in the array
	 * @return the array
	 */
	public static double[] zeros(int length) {
		double[] arr = new double[length];
		for(int i = 0; i < length; ++i) {
			arr[i] = 0;
		}
		return arr;
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
	
	/**
	 * Sums the values in the array.
	 * @param arr the values to sum
	 * @return the sum
	 */
	public static double sum(double[] arr) {
		double out = 0;
		for(double val : arr) {
			out += val;
		}
		return out;
	}
	
	/**
	 * Calculates the element wise subtraction difference of two vectors, v1-v2.
	 * @param v1 the first vector
	 * @param v2 the second vector
	 * @return v1-v2
	 */
	public static double[] vvSub(double[] v1, double[] v2) {
		int length = v1.length;
		if(v2.length != length) {
			System.err.println("Error: input vectors must be the same length.");
			return null;
		}
		
		double[] v3 = new double[length];
		for(int i = 0; i < length; ++i) {
			v3[i] = v1[i] - v2[i];
		}
		return v3;
	}
	
	/**
	 * Calculates the element wise multiplation of a scalor and a vector.
	 * @param v
	 * @param s
	 * @return
	 */
	public static double[] svMul(double s, double[] v) {
		double[] res = new double[v.length];
		for(int i = 0; i < v.length; ++i) {
			res[i] = s*v[i];
		}
		return res;
	}

	/**
	 * Returns the one hot encoding of
	 * @param length
	 * @param hot
	 * @return
	 */
	public static double[] onehot(int length, int hot) {
		double[] arr = zeros(length);
		arr[hot] = 1.0;
		return arr;
	}
}
