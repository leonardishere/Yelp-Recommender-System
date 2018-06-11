import java.io.Serializable;

public class NeuralNetwork implements Serializable {

	/**
     * 
     */
    private static final long serialVersionUID = 4276308331369683669L;

    //public InputLayer inputLayer;
	//public HiddenLayer[] hiddenLayers;
	//public OutputLayer outputLayer;
	//public T[] trainingData;
	//public ArrayList<T> trainingData, testingData;
    public Layer[] layers;
    public int iters = 0;
    
	public int numInputs, numOutputs;

	/*
	public NeuralNetwork(int[] layerSizes) {
		numInputs = layerSizes[0];
		inputLayer = new InputLayer(numInputs);

		Random rand = new Random();

		hiddenLayers = new HiddenLayer[layerSizes.length-2];
		for(int i = 0; i < hiddenLayers.length; ++i) {
			hiddenLayers[i] = new HiddenLayer(layerSizes[i+1]);

			if(i == 0) hiddenLayers[i].connectPrevLayer(inputLayer);
			else hiddenLayers[i].connectPrevLayer(hiddenLayers[i-1]);

			hiddenLayers[i].randomizeWeights(rand);
		}

		numOutputs = layerSizes[layerSizes.length-1];
		outputLayer = new OutputLayer(numOutputs);
		outputLayer.connectPrevLayer(hiddenLayers.length == 0 ? inputLayer : hiddenLayers[hiddenLayers.length-1]);
		outputLayer.randomizeWeights(rand);

		trainingData = new ArrayList<>();
		testingData = new ArrayList<>();
	}
	*/
	
	public NeuralNetwork(Layer[] layers) {
	    this.layers = layers;
	    for(int i = 1; i < layers.length; ++i) {
	        layers[i].connectPrevLayer(layers[i-1]);
	    }
	}

	/*
	public void addTrainingData(T[] data) {
		trainingData = data;
		//Collections.shuffle(Arrays.asList(trainingData));
	}
	 */
	/*
	public void addTrainingData(ArrayList<T> data) {
		trainingData.addAll(data);
	}

	public void addTestingData(ArrayList<T> data) {
		testingData.addAll(data);
	}

	public double[] errorVector() {
		double[] errorVector = new double[numOutputs];
		Arrays.fill(errorVector, 0);

		if(trainingData == null) {
			return errorVector;
		}

		//System.out.println("Errors by instance:\n");
		for(T t : trainingData) {
			double[] target = t.getOutputs();
			double[] output = feedForward(t);
			double[] diff = Helper.vectorSub(target, output);
			double[] diffSq = Helper.vectorPow(diff, 2);
			errorVector = Helper.vectorAdd(errorVector, diffSq);

			/*
			System.out.printf(
					"inputs: %s. nn output: %s. diff: %s\n", 
					Arrays.toString(t.getInputs()), 
					Arrays.toString(output), 
					Arrays.toString(diff));
			 
		}
		errorVector = Helper.vectorMul(errorVector, 0.5);
		//System.out.printf("Error vector:\n%s\n", Arrays.toString(errorVector));
		return errorVector;
	}

	public double errorScalor() {
		//this should work, hypothetically. it relies on the summations in the equation to be swappable, which they should be
		double sum = Helper.sum(errorVector());
		//System.out.printf("Error scalor:\n%f\n", sum);
		return sum;
	}
    */

	/**
	 * Performs stochastic gradient decent.
	 * Uses batch size = 1 for now.
	 * @param numIters
	 * @param learningRate
	 */
	/*
	public void sgd(int numIters, double learningRate, int printEvery) {
		//T[] trainingData2 = trainingData.clone();
		ArrayList<T> trainingData2 = new ArrayList<>();
		trainingData2.addAll(trainingData);

		double prevBest = Double.POSITIVE_INFINITY;
		for(int i = 0; i < numIters; ++i) {
			if(i % printEvery == 0) {
				double error = errorScalor();
				System.out.printf("iter = %8d, error = %f, acc = %f %s\n", i, error, classificationAccuracy(), error < prevBest ? "--" : "");
				prevBest = Math.min(prevBest, error);
			}
			//Collections.shuffle(Arrays.asList(trainingData2));
			Collections.shuffle(trainingData2);
			for(T t : trainingData2) {
				double[] target = t.getOutputs();
				double[] output = feedForward(t);

				double[] diff = Helper.vectorSub(target, output);
				double[] oneMinus = Helper.vectorSub(1, output);
				double[] outputError = Helper.vectorMul(Helper.vectorMul(output, oneMinus), diff);
				outputLayer.setErrors(outputError);
				outputLayer.backPropagate();

				for(HiddenLayer hl : hiddenLayers) {
					hl.adjustWeights(learningRate);
				}
				outputLayer.adjustWeights(learningRate);
			}
		}
		double error = errorScalor();
		System.out.printf("iter = %8d, error = %f\n", numIters, error, classificationAccuracy(), error < prevBest ? "--" : "");
		System.out.printf("error:  %s\n", Helper.toString(errorVector()));
	}

	public double[] feedForward(T t) {
		inputLayer.setInputs(t.getInputs());
		for(int i = 0; i < hiddenLayers.length; ++i) {
			hiddenLayers[i].calculateOutput();
		}
		outputLayer.calculateOutput();
		return outputLayer.getOutput();
	}

	public int classify(T t) {
		double[] output = feedForward(t);
		int maxIndex = Helper.maxIndex(output);
		return maxIndex;
	}

	public void printResults() {
		for(T t : trainingData) {
			double[] target = t.getOutputs();
			double[] output = feedForward(t);
			double[] diff = Helper.vectorSub(target, output);

			System.out.printf(
					"inputs: %s. nn output: %s. diff: %s\n", 
					Helper.toString(t.getInputs()), 
					Helper.toString(output), 
					Helper.toString(diff)
					);
		}
	}

	public double accuracy() {
		if(testingData.isEmpty()) return 0.0;
		double sum = 0;
		for(T t : testingData) {
			double[] target = t.getOutputs();
			double[] output = feedForward(t);
			double[] roundedOutput = Helper.round(output);
			double[] sub = Helper.vectorSub(roundedOutput, target);
			double[] diff = Helper.abs(sub);
			double diffSum = Helper.sum(diff);
			sum += diffSum;
		}
		double error = sum / (testingData.size() * testingData.get(0).getNumOutputs());
		double acc = 1 - error;
		return acc;
	}

	public double classificationAccuracy() {
		if(testingData.isEmpty()) return 0.0;
		int correct = 0;
		for(T t : testingData) {
			double[] target = t.getOutputs();
			double[] output = feedForward(t);
			int class1 = Helper.maxIndex(target);
			int class2 = Helper.maxIndex(output);
			if(class1 == class2) correct++;
		}
		double acc = correct * 1.0 / testingData.size();
		return acc;
	}
	*/
	
	public void feedforward(double[] inputs) {
	    ((InputLayer) layers[0]).setInputs(inputs);
	    for(int i = 1; i < layers.length; ++i) {
	        layers[i].calculateOutput();
	    }
	}
	
	public double getOutput() {
	    double output = layers[layers.length-1].getNeurons()[0].getOutput();
	    //if(output < 1) output = 1;
	    //if(output > 5) output = 5;
	    return output;
	}
	
	public void backpropagate(double target, double eta) {
	    double diff = target-getOutput();
        //double[] oneMinus = Helper.vectorSub(1, output);
        //double[] outputError = Helper.vectorMul(Helper.vectorMul(output, oneMinus), diff);
        //outputLayer.setErrors(outputError);
        //outputLayer.backPropagate();
	    ((LinearOutputLayer) layers[layers.length-1]).setErrors(new double[] {diff});
	    ((LinearOutputLayer) layers[layers.length-1]).backPropagate();

	    /*
        for(HiddenLayer hl : hiddenLayers) {
            hl.adjustWeights(learningRate);
        }
        */
	    for(int i = 1; i < layers.length-1; ++i) {
	        ((SigmoidLayer) layers[i]).adjustWeights(eta);
	    }
        //outputLayer.adjustWeights(learningRate);
        ((LinearOutputLayer) layers[layers.length-1]).adjustWeights(eta);
	}
	
	public void sgd(double[] inputs, double output, double eta) {
	    feedforward(inputs);
	    backpropagate(output, eta);
	}
}
