import java.io.Serializable;

public interface Layer extends Serializable{
	int width();
	
	Neuron[] getNeurons();

    void connectPrevLayer(Layer layer);
    
    void calculateOutput();
}
