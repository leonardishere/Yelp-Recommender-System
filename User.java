import java.util.HashMap;
import java.util.Map;

public class User {
  private String id;
  private Map<String, Integer> map = new HashMap();

  public User(String userid){
    this.id = userid;
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
    
    //return individual rating for a business
  public int GetRating(String businessID){
    return ratingmap.get(businessID);
  }

}
