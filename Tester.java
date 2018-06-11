import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;

public class Tester {

    public static void sizeTest() {
        Connection conn = DatabaseReader.connect_sqlite(true);
        User[] users = DatabaseReader.loadUsers(conn, true);
        KeyMap userMap = new KeyMap();
        int numUsers = users.length;
        for(int i = 0; i < numUsers; ++i) {
            userMap.add(users[i].id);
        }
        ArrayList<UserBusinessInteraction> interactions = DatabaseReader.loadAllReviews(conn, true, true);
        for(int i = 0; i < interactions.size(); ++i) {
            UserBusinessInteraction interaction = interactions.get(i);
            users[userMap.convert(interaction.userID)].addRating(interaction.businessID, interaction.rating);
        }
        
        /*
        int numPairs = 0;
        for(User user : users) {
            int numRatings = user.numRatings();
            numPairs += (numRatings)*(numRatings-1)/2;
        }
        System.out.printf("num pairs: %d\n", numPairs);
        */
        HashSet<StringPair> pairSet = new HashSet<>();
        for(User user : users) {
            UserBusinessInteraction[] interactions2 = user.getInteractions();
            for(int i = 0; i < interactions2.length-1; ++i) {
                for(int j = i+1; j < interactions2.length; ++j) {
                    pairSet.add(new StringPair(interactions2[i].businessID, interactions2[j].businessID));
                }
            }
        }
        System.out.printf("num pairs: %d\n", pairSet.size());
    }
    
    public static void main(String[] args) {
        sizeTest();
    }

}
