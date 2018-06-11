/**
 * SortObject is a class used to sort Businesses by their similarity to another Business.
 * This is used in KNN.
 *
 */
public class SortObject implements Comparable<SortObject>{
    /**
     * The Business to sort.
     */
    public Business business;
    /**
     * The similarity between this Business and other.
     */
    public double value;
    
    /**
     * Constructs a new SortObject.
     * @param business the business to store
     * @param value the double to store
     */
    public SortObject(Business business, double value){
        this.business = business;
        this.value = value;
    }
    
    /**
     * Compares this to another SortObject using the distance.
     * @param other the object to compare
     * @return a value used to sort
     */
    public int compareTo(SortObject other){
        if(value == other.value) return 0;
        if(value > other.value) return 1;
        return -1;
    }
}
