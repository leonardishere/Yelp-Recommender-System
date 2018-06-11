public class InputNeuron implements Neuron{

    /**
     * 
     */
    private static final long serialVersionUID = 9205288107118304807L;
    public double output;
	
	public InputNeuron() {
		output = 0;
	}
	
	public void setInput(double input) {
		output = input;
	}
	
	public double getOutput() {
		return output;
	}
}
