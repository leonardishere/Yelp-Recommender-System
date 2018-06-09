import java.io.Serializable;

public class Model implements Serializable{
	
	public static final int NUM_STARS = 5, NUM_FEATURES = 41;
	private static final double[] MULTIPLIERS = new double[] {1,2,3,4,5};
	private double[][] weights = null;
	private double[] biases = null;
	private double[][] m0 = null, m1 = null;
	private double[] v2 = null, v3 = null, v4 = null, v7 = null, v8 = null;
	private double s5 = 0, s6 = 0, s9 = 0;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6587042410056123286L;

	public Model() {
		weights = Helper.rand(NUM_STARS, NUM_FEATURES, 0.0, 0.1);
		biases = Helper.rand(NUM_STARS, 0.0, 0.1);
	}
	
	public void feedforward(double[][] input) {
		m0 = input;										//assembled layer: store input
		m1 = Helper.elementalMMmul(input, weights); 	//summing layer: multiply assembled layer * weights 
		v2 = Helper.rowSum(m1); 						//summing layer: sum by row
		v3 = Helper.vvAdd(v2, biases); 					//summing layer: add biases
		v4 = Helper.removeNegatives(v3);				//normalization layer: remove negatives
		s5 = Helper.sum(v4);							//normalization layer: find sum
		s6 = s5==0? 0 : 1/s5;							//normalization layer: find scale
		v7 = Helper.svMul(s6, v4);						//normalization layer: normalize
		v8 = Helper.vvMul(v7, MULTIPLIERS);				//output layer: multiply multiplier * confidence
		s9 = Helper.sum(v8);							//output layer: sum
	}
	
	public void backpropagate(double[] target, double eta) {
		double[] e1 = Helper.vvSub(target, v7);			//output layer: find error
		double[] e2 = Helper.svMul(s5, e1);				//normalization layer: scale error
		double[] e3 = Helper.svMul(eta, e2);			//normalization layer: scale by learning rate
		double[][] e4 = Helper.vmMul(e3, m0);			//summing layer: scale error by each input
		//double[][] e3 = Helper.vmMul(e2, m0);			//summing layer: scale error by each input
		//double[][] e4 = Helper.smMul(eta, e3);		//summing layer: scale by learning rate
		weights = Helper.mmAdd(weights, e4);			//summing layer: adjust weights
		biases = Helper.vvAdd(biases, e3);				//summing layer: adjust biases
	}
	
	public double[] classOutput() {
		return v7;
	}
	
	public double numericalOutput() {
		return s9;
	}
	
	public void sgd(double[][] input, int target, double eta) {
		feedforward(input);
		backpropagate(Helper.onehot(NUM_STARS, target), eta);
	}
}
