import java.util.HashMap;
import java.util.Map;

public class User {

    Map<String, Integer> map;

    public User(){
        map = new HashMap<>();
    }

    /**
     * Adds the business and rating to a map
     * @param businessID
     * @param rating
     */
    public void addRating(String businessID, int rating){
        map.put(businessID, rating);
    }

    /**
     * @return Ratings map
     */
    public Map<String, Integer> getRatings(){
        return this.map;
    }

}
