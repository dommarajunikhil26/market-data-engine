package com.nikhil.marketdataengine.mockTests;

import com.nikhil.marketdataengine.utils.AverageTracker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AverageTrackerTest {

    @Test
    public void testInit() {
        AverageTracker averageTracker = new AverageTracker(0.0, 0.0);

        assertEquals(0.0, averageTracker.getAverage(), "Initial average should be 0.0");
    }

    @Test
    public void testAfterOneUpdate() {
        AverageTracker averageTracker = new AverageTracker(0.0, 0.0);
        AverageTracker updatedAverage = averageTracker.next(10);
        assertEquals(10.0, updatedAverage.getAverage(), "Initial average should be 10.0");
    }

    @Test
    public void testAfterThreeUpdates(){
        AverageTracker averageTracker = new AverageTracker(0.0, 0.0);
        AverageTracker updatedAverage = averageTracker
                .next(1)
                .next(2)
                .next(3);
        assertEquals(2.0, updatedAverage.getAverage(), "Initial average should be 2.0");
    }

    @Test
    public void testDivisionByZeroReturnsZeroNotNaN() {
        AverageTracker averageTracker = new AverageTracker(100.0, 0.0);
        assertEquals(0.0, averageTracker.getAverage(), "Initial average should be 0.0");
    }
}
