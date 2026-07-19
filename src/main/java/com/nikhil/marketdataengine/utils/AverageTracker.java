package com.nikhil.marketdataengine.utils;

public class AverageTracker {
    private final double sum;
    private final double count;

    public AverageTracker(double sum, double count){
        this.sum = sum;
        this.count = count;
    }

    public AverageTracker next(double price) {
        return new AverageTracker(sum+price, count+1);
    }

    public double getAverage() {
        return count == 0 ? 0.0 : sum/count;
    }
}
