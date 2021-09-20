package main.calculator;

import java.util.HashMap;

public class CountResult {
    public HashMap<String, HashMap<String, Integer>> statistic;
    public HashMap<String, Integer> total;

    public CountResult(HashMap<String, HashMap<String, Integer>> statistic, HashMap<String, Integer> total) {
        this.statistic = statistic;
        this.total = total;
    }
}
