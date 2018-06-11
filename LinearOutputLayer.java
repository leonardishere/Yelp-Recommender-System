import java.util.Arrays;
import java.util.Random;

public class LinearOutputLayer implements Layer {
    /**
     * 
     */
    private static final long serialVersionUID = 2606388804499219484L;
    public LinearNeuron[] linearNeurons;
    public double[] errors;
    
    public Layer prevLayer;
    
    public LinearOutputLayer() {
        int layerWidth = 1;
        linearNeurons = new LinearNeuron[layerWidth];
        for(int i = 0; i < layerWidth; ++i) {
            linearNeurons[i] = new LinearNeuron();
        }
        errors = new double[layerWidth];
        Arrays.fill(errors, 0);
        prevLayer = null;
    }
    
    public int width() {
        return linearNeurons.length;
    }

    @Override
    public Neuron[] getNeurons() {
        return linearNeurons;
    }
    
    //assuming fully connected
    public void connectPrevLayer(Layer layer) {
        prevLayer = layer;
        int thisWidth = linearNeurons.length;
        Neuron[] prevNeurons = layer.getNeurons();
        
        for(int i = 0; i < thisWidth; ++i) {
            linearNeurons[i].setPrevNeurons(prevNeurons);
        }
    }

    public void calculateOutput() {
        for(LinearNeuron sn : linearNeurons) {
            sn.calculateOutput();
        }
    }
    
    public double[] getOutput() {
        double[] output = new double[linearNeurons.length];
        for(int i = 0; i < linearNeurons.length; ++i) {
            output[i] = linearNeurons[i].getOutput();
        }
        return output;
    }
    
    public void randomizeWeights(Random rand) {
        for(LinearNeuron sn : linearNeurons) {
            sn.randomizeWeights(rand);
        }
    }
    
    public void setErrors(double[] errors) {
        this.errors = errors;
    }
    
    public void adjustWeights(double learningRate) {
        for(int i = 0; i < linearNeurons.length; ++i) {
            linearNeurons[i].adjustWeights(learningRate, errors[i]);
        }
        Arrays.fill(errors, 0);
    }
    
    public void backPropagate() {
        //TODO: double check logic
        if(prevLayer instanceof SigmoidLayer) {
            SigmoidLayer prev = (SigmoidLayer) prevLayer;
            
            double[] output = prev.getOutput();
            double[] oneMinus = Helper.svSub(1, output);
            double[] weightErrorSum = new double[prev.width()];
            Arrays.fill(weightErrorSum, 0);
            
            for(int i = 0; i < prevLayer.width(); ++i) {
                for(int j = 0; j < width(); ++ j) {
                    try {
                        weightErrorSum[i] += linearNeurons[j].getWeights()[i] * errors[j];
                    } catch(ArrayIndexOutOfBoundsException ex) {
                        System.err.printf(
                            "i=%d, j=%d, wes.l=%d, sn.l=%d, snj.gw.l=%d, e.l=%d\n",
                            i, j, weightErrorSum.length, linearNeurons.length, linearNeurons[j].getWeights().length, errors.length
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
