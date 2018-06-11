import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

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
     * Calculates the element wise addition of two vectors, v1+v2.
     * @param v1 the first vector
     * @param v2 the second vector
     * @return v1+v2
     */
    public static double[] vvAdd(double[] v1, double[] v2) {
        int length = v1.length;
        if(v2.length != length) {
            System.err.println("Error: input vectors must be the same length.");
            return null;
        }

        double[] v3 = new double[length];
        for(int i = 0; i < length; ++i) {
            v3[i] = v1[i] + v2[i];
        }
        return v3;
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
	 * Calculates the element wise multiplication of two vectors, v1*v2.
	 * @param v1 the first vector
	 * @param v2 the second vector
	 * @return v1*v2
	 */
	public static double[] vvMul(double[] v1, double[] v2) {
		int length = v1.length;
		if(v2.length != length) {
			System.err.println("Error: input vectors must be the same length.");
			return null;
		}

		double[] v3 = new double[length];
		for(int i = 0; i < length; ++i) {
			v3[i] = v1[i] * v2[i];
		}
		return v3;
	}

    /**
     * Calculates the element wise division of two vectors, v1/v2.
     * @param v1 the first vector
     * @param v2 the second vector
     * @return v1/v2
     */
    public static double[] vvDiv(double[] v1, double[] v2) {
        int length = v1.length;
        if(v2.length != length) {
            System.err.println("Error: input vectors must be the same length.");
            return null;
        }

        double[] v3 = new double[length];
        for(int i = 0; i < length; ++i) {
            if(v2[i] == 0) v3[i] = 0;
            else v3[i] = v1[i] / v2[i];
        }
        return v3;
    }

    /**
     * Calculates the element wise subtraction of a scalor and a vector.
     * @param v
     * @param s
     * @return
     */
    public static double[] svSub(double s, double[] v) {
        double[] res = new double[v.length];
        for(int i = 0; i < v.length; ++i) {
            res[i] = s-v[i];
        }
        return res;
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
	 * Calculates the element wise multiplication of a vector and a matrix.
	 * The vector is of size s1 and the matrix of size s1*s2. Every item in row r will be multiplied by the value at the correspond row in v when v is represented as a column vector.
	 * @param v
	 * @param m
	 * @return
	 */
	public static double[][] vmMul(double[] v, double[][] m){
		double[][] res = new double[m.length][m[0].length];
		for(int row = 0; row < m.length; ++row) {
			for(int col = 0; col < m[row].length; ++col) {
				res[row][col] = v[row] * m[row][col];
			}
		}
		return res;
	}

	/**
	 * Calculates the element wise multiplication of a scalor and a matrix.
	 * @param s
	 * @param m
	 * @return
	 */
	public static double[][] smMul(double s, double[][] m){
		double[][] res = new double[m.length][m[0].length];
		for(int row = 0; row < m.length; ++row) {
			for(int col = 0; col < m[row].length; ++col) {
				res[row][col] = s * m[row][col];
			}
		}
		return res;
	}

	/**
	 * Adds the two matrices element by element.
	 * @param m1 the first matrix
	 * @param m2 the second matrix
	 * @return the sum of the matrices
	 */
	public static double[][] mmAdd(double[][] m1, double[][] m2){
		if(m1 == null || m2 == null) return null;
		if(m1.length != m2.length || m1[0].length != m2[0].length) {
			System.err.println("Error: the matrix arguments to Helper.elementalMMmul must be equal size.");
			return null;
		}
		double[][] res = new double[m1.length][m1[0].length];
		for(int row = 0; row < m1.length; ++row) {
			for(int col = 0; col < m1[0].length; ++col) {
				res[row][col] = m1[row][col] + m2[row][col];
			}
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

	public static int unionSize(ArrayList<String> arr1, ArrayList<String> arr2){
		HashSet<String> set = new HashSet<>();
		set.addAll(arr1);
		set.addAll(arr2);
		return set.size();
	}

	public static int junctionSize(ArrayList<String> arr1, ArrayList<String> arr2) {
		HashSet<String> set = new HashSet<>();
		for(String str1 : arr1) {
			if(arr2.contains(str1)) {
				set.add(str1);
			}
		}
		return set.size();
	}

	public static double jaccardSimilarity(ArrayList<String> arr1, ArrayList<String> arr2) {
		return junctionSize(arr1, arr2)*1.0/unionSize(arr1, arr2);
	}

	/**
	 * Creates a new matrix populated with random values.
	 * @param numRows the number of rows of the matrix
	 * @param numCols the number of cols of the matrix
	 * @param min the minimal random value
	 * @param max the maximal random value
	 * @return the randomized matrix
	 */
	public static double[][] rand(int numRows, int numCols, double min, double max){
		double diff = max-min;
		Random rand = new Random();
		double[][] res = new double[numRows][numCols];
		for(int row = 0; row < numRows; ++row) {
			for(int col = 0; col < numCols; ++col) {
				res[row][col] = rand.nextDouble()*diff - min;
			}
		}
		return res;
	}

	/**
	 * Creates a new array populated with random values.
	 * @param numRows the number of elements of the array
	 * @param min the minimal random value
	 * @param max the maximal random value
	 * @return the randomized array
	 */
	public static double[] rand(int numRows, double min, double max){
		double diff = max-min;
		Random rand = new Random();
		double[] res = new double[numRows];
		for(int row = 0; row < numRows; ++row) {
			res[row] = rand.nextDouble()*diff - min;
		}
		return res;
	}

	/**
	 * Multiplies the two matrices element by element.
	 * @param m1 the first matrix
	 * @param m2 the second matrix
	 * @return the product of the matrices
	 */
	public static double[][] elementalMMmul(double[][] m1, double[][] m2){
		if(m1 == null || m2 == null) return null;
		if(m1.length != m2.length || m1[0].length != m2[0].length) {
			System.err.println("Error: the matrix arguments to Helper.elementalMMmul must be equal size.");
			return null;
		}
		double[][] res = new double[m1.length][m1[0].length];
		for(int row = 0; row < m1.length; ++row) {
			for(int col = 0; col < m1[0].length; ++col) {
				res[row][col] = m1[row][col] * m2[row][col];
			}
		}
		return res;
	}

	/**
	 * Sums up each row of a matrix into an array.
	 * @param m the matrix to sum
	 * @return the row-wise sum
	 */
	public static double[] rowSum(double[][] m) {
		double[] res = new double[m.length];
		for(int row = 0; row < m.length; ++row) {
			res[row] = 0;
			for(int col = 0; col < m[row].length; ++col) {
				res[row] += m[row][col];
			}
		}
		return res;
	}

	/**
	 * All negatives become zero.
	 * @param v
	 * @return
	 */
	public static double[] removeNegatives(double[] v) {
		double[] res = new double[v.length];
		for(int i = 0; i < v.length; ++i) {
			res[i] = v[i] > 0 ? v[i] : 0;
		}
		return res;
	}

	/**
	 * Adds the row to the row in the matrix. Does this in-place.
	 * @param m
	 * @param row
	 * @param rowNum
	 */
	public static void inplaceAddRow(double[][] m, double[] row, int rowNum) {
		for(int i = 0; i < row.length; ++i) {
			m[rowNum][i] += row[i];
		}
	}

	public static double length(double[] v) {
	    double sum = 0;
	    for(int i = 0; i < v.length; ++i) {
	        sum += v[i]*v[i];
	    }
	    return Math.sqrt(sum);
	}
	
	public static double[] softmax(double[] v) {
	    double sum = 0;
	    double[] res = new double[v.length];
	    for(int i = 0; i < v.length; ++i) {
	        double exp = Math.exp(v[i]);
	        res[i] = exp;
	        sum += exp;
	    }
	    for(int i = 0; i < v.length; ++i) {
	        res[i] /= sum;
	    }
	    return res;
	}
	
	public static double[][] softmaxGradient(double[] v){
	    double[][] res = new double[v.length][v.length];
	    for(int i = 0; i < v.length; ++i) {
	        for(int j = 0; j < v.length; ++j) {
	            if(i == j) res[i][j] = v[i]*(1-v[j]);
	            else res[i][j] = -(v[i]*v[j]);
	        }
	    }
	    return res;
	}
	
	public static double[] flatten(double[][] m) {
	    double[] res = new double[m.length*m[0].length];
	    int x = 0;
	    for(int i = 0; i < m.length; ++i) {
	        for(int j = 0; j < m[i].length; ++j) {
	            res[x] = m[i][j];
	            ++x;
	        }
	    }
	    return res;
	}
	
	public static double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }
}
