import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import java.util.HashMap;
import java.util.Map;

public class POSTagger {
    MaxentTagger tagger =  new MaxentTagger("lib/english-left3words-distsim.tagger");

    private static final String NOUN = "N"; // Starts with N, handles NN, NNS, NNP, NNPS
    private static final String ADJECTIVE = "J"; // Starts with J, handles JJ, JJR, JJS

    public POSTagger() {}

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

}
