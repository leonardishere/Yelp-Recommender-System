import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class User {
	public String id;
	public Map<String, Integer> map = new HashMap<>();

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
	public int getRating(String businessID){
		return map.get(businessID);
	}

	public int numRatings() {
		return map.size();
	}

	public UserBusinessInteraction[] getInteractions() {
		UserBusinessInteraction[] interactions = new UserBusinessInteraction[map.size()];
		Set<Entry<String, Integer> > ratingsSet = map.entrySet();
		@SuppressWarnings("rawtypes")
		Entry[] entries = new Entry[ratingsSet.size()];
		ratingsSet.toArray(entries);
		for(int i = 0; i < map.size(); ++i) {
			interactions[i] = new UserBusinessInteraction(id, (String)entries[i].getKey(), (int)entries[i].getValue());
		}
		return interactions;
	}

	public boolean equals(User other) {
		return id.equals(other.id);
	}
}
