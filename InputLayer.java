public class InputLayer implements Layer {

	/**
     * 
     */
    private static final long serialVersionUID = 4263836448645831883L;
    public InputNeuron[] inputNeurons;
	
	public InputLayer(int layerWidth) {
		inputNeurons = new InputNeuron[layerWidth];
		for(int i = 0; i < layerWidth; ++i) {
			inputNeurons[i] = new InputNeuron();
		}
		//nextLayer = null;
	}
	
	public int width() {
		return inputNeurons.length;
	}
	
	public void setInputs(double[] inputs) {
		if(inputNeurons.length != inputs.length) {
			//System.err.println("Error: the input vectors must be equal dimensions.");
			System.err.printf("Error: the input vectors must be equal dimensions. (%d v %d)\n", inputNeurons.length, inputs.length);
			return;
		}
		int length = inputNeurons.length;
		for(int i = 0; i < length; ++i) {
			inputNeurons[i].setInput(inputs[i]);
		}
	}
	public Neuron[] getNeurons() {
		return inputNeurons;
	}

    @Override
    public void connectPrevLayer(Layer layer) {}

    @Override
    public void calculateOutput() {}
}
