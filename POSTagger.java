import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import java.util.HashMap;
import java.util.Map;

public class POSTagger {
	MaxentTagger tagger = null;
	
    private static final String NOUN = "N"; // Starts with N, handles NN, NNS, NNP, NNPS
    private static final String ADJECTIVE = "J"; // Starts with J, handles JJ, JJR, JJS

    public POSTagger() {
    	//I had to go with this type of structure because my path is different and I didn't want to mess up other peoples builds by just switching it
    	try {
    		tagger =  new MaxentTagger("lib/english-left3words-distsim.tagger");
    	} catch(Exception ex) {
    		tagger = null;
    	}
    	if(tagger == null) {
    		tagger = new MaxentTagger("yelp_recommender_system/lib/english-left3words-distsim.tagger");
    	}
    }

    public Map<String, Integer> tagPOS(String sentence){
        Map<String, Integer> map = new HashMap<>();

        String taggedString = tagger.tagString(sentence);

        // Separate into <word>_<tag>
        String[] splitTag = taggedString.split(" ");

        for (String splitWord : splitTag){
            // Split <word>_<tag>
            int lastInst = splitWord.lastIndexOf("_");
            String word = splitWord.substring(0, lastInst);
            String tag = splitWord.substring(lastInst + 1, splitWord.length());

            // Check if noun or adjective
            if (tag.startsWith(NOUN) || tag.startsWith(ADJECTIVE)){
                int count = map.containsKey(word) ? map.get(word) : 0;
                map.put(word, count + 1);
            }
        }

        return map;
    }
    
    /**
     * Tester function
     * @param args
     */
    public static void main(String[] args) {
    	POSTagger posTagger = new POSTagger();
    	String sentence = "The food was very good. The service was good. The service was slow.";
        System.out.println("\""+sentence+"\""+" = > "+posTagger.tagPOS(sentence).toString());
    }

}
