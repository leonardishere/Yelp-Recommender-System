import java.util.Arrays;
import java.util.Random;

public class SigmoidNeuron implements Neuron{

	/**
     * 
     */
    private static final long serialVersionUID = 3167521537930579498L;
    public Neuron[] prevNeurons; //including nextNeurons doesnt help. opting in favor of receiving weights and errors
	//public Neuron[] outputs;
	public double w0;
	public double[] weights; //input weights
	public double output;
	public double error; //for gradient descent
	
	//public double[] nextWeights, nextErrors;
	public double nextError;
	
	public SigmoidNeuron() {
		prevNeurons = null;
		weights = null;
		w0 = 0;
		output = 0;
		error = 0;
		
		//nextWeights = null, nextErrors = null;
		nextError = 0;
	}
	
	public void setPrevNeurons(Neuron[] neurons) {
		prevNeurons = neurons;
		weights = new double[neurons.length];
		Arrays.fill(weights, 0);
	}
	
	public Neuron[] getPrevNeurons() {
		return prevNeurons;
	}
	
	public void calculateOutput() {
		output = w0;
		if(prevNeurons != null) {
			for(int i = 0; i < prevNeurons.length; ++i) {
				output += prevNeurons[i].getOutput() * weights[i];
			}
		}
		output = Helper.sigmoid(output);
	}
	
	public double getOutput() {
		return output;
	}
	
	public void randomizeWeights(Random rand) {
		w0 = rand.nextGaussian();
		if(weights != null) {
			for(int i = 0; i < weights.length; ++i) {
				weights[i] = rand.nextGaussian() / 2.0;
			}
		}
	}
	
	public void adjustWeights(double learningRate, double error) {
		double mul = learningRate * error;
		w0 += mul;
		for(int i = 0; i < prevNeurons.length; ++i) {
			weights[i] += mul * prevNeurons[i].getOutput();
		}
	}
	
	/*
	public void setNextLayerSize(int size) {
		nextWeights = new double[size];
		Arrays.fill(nextWeights, 0);
		nextErrors = new double[size];
		Arrays.fill(nextErrors, 0);
	}
	*/
	
	public void addError(double error) {
		nextError += error;
	}
	
	public double[] getWeights() {
		return weights.clone();
	}
}
