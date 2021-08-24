package object;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.SmithWatermanGotoh;

public class Algorithm {
    public static float caculatorDistence(String keyVar, String keyVar1) {
        AbstractStringMetric metric3 = new SmithWatermanGotoh();
        try {
            float result3 = metric3.getSimilarity(keyVar, keyVar1);
            return result3;
        } catch (Exception e) {
            return -1;
        }

    }

    public static void main(String[] args) {
        float score = Algorithm.caculatorDistence(">", ">=");
        System.out.println(score);
    }
}
