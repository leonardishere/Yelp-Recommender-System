import java.util.Arrays;
import java.util.Random;

public class SigmoidLayer implements Layer {

	/**
     * 
     */
    private static final long serialVersionUID = 6974613587587102313L;
    public SigmoidNeuron[] sigmoidNeurons;
	//public Layer prevLayer, nextLayer;
	public Layer prevLayer;
	public double[] errors;
	
	public SigmoidLayer(int layerWidth) {
		sigmoidNeurons = new SigmoidNeuron[layerWidth];
		for(int i = 0; i < layerWidth; ++i) {
			sigmoidNeurons[i] = new SigmoidNeuron();
		}
		prevLayer = null;
	}
	
	public int width() {
		return sigmoidNeurons.length;
	}
	
	//assuming fully connected
	public void connectPrevLayer(Layer layer) {
		prevLayer = layer;
		int thisWidth = sigmoidNeurons.length;
		Neuron[] prevNeurons = layer.getNeurons();
		
		for(int i = 0; i < thisWidth; ++i) {
			sigmoidNeurons[i].setPrevNeurons(prevNeurons);
		}
	}

	@Override
	public Neuron[] getNeurons() {
		return sigmoidNeurons;
	}

	public void calculateOutput() {
		for(SigmoidNeuron sn : sigmoidNeurons) {
			sn.calculateOutput();
		}
	}
	
	public double[] getOutput() {
		double[] output = new double[sigmoidNeurons.length];
		for(int i = 0; i < sigmoidNeurons.length; ++i) {
			output[i] = sigmoidNeurons[i].getOutput();
		}
		return output;
	}
	
	public void randomizeWeights(Random rand) {
		for(SigmoidNeuron sn : sigmoidNeurons) {
			sn.randomizeWeights(rand);
		}
	}
	
	public void setErrors(double[] errors) {
		this.errors = errors;
	}
	
	public void adjustWeights(double learningRate) {
		for(int i = 0; i < sigmoidNeurons.length; ++i) {
			try {
				sigmoidNeurons[i].adjustWeights(learningRate, errors[i]);
			} catch(ArrayIndexOutOfBoundsException ex) {
				System.err.printf(
					"i=%d, sn.l=%d, e.l=%d\n",
					i, sigmoidNeurons.length, errors.length
				);
				ex.printStackTrace();
			}
		}
		Arrays.fill(errors, 0);
	}
	
	public void backPropagate() {
		if(prevLayer instanceof SigmoidLayer) {
		    SigmoidLayer prev = (SigmoidLayer) prevLayer;
			
			double[] output = prev.getOutput();
			double[] oneMinus = Helper.svSub(1, output);
			double[] weightErrorSum = new double[prev.width()];
			Arrays.fill(weightErrorSum, 0);
			
			for(int i = 0; i < prevLayer.width(); ++i) {
				for(int j = 0; j < width(); ++ j) {
					try {
						weightErrorSum[i] += sigmoidNeurons[j].getWeights()[i] * errors[j];
					} catch(ArrayIndexOutOfBoundsException ex) {
						System.err.printf(
							"i=%d, j=%d, wes.l=%d, sn.l=%d, snj.gw.l=%d, e.l=%d\n",
							i, j, weightErrorSum.length, sigmoidNeurons.length, sigmoidNeurons[j].getWeights().length, errors.length
						);
						ex.printStackTrace();
					}
				}
			}
			
			double[] prevErrors = Helper.vvMul(Helper.vvMul(output, oneMinus), weightErrorSum);
			//System.err.printf("pe.l=%d\n", prevErrors.length);
			
			prev.setErrors(prevErrors);
			prev.backPropagate();
		}
	}
}
