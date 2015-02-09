package com.benbarron.react;

import com.google.caliper.Benchmark;
import org.junit.Test;

public class Ben extends Benchmark {

    private final Integer[] array = new Integer[100000];

    public void timeForEach(long reps) {
        for (int i = 0; i < reps; i++) {
            for (Integer j : array) {

            }
        }
    }

    public void timeFor(long reps) {
        for (int i = 0; i < reps; i++) {
            for (int j = 0; i < array.length; i++) {

            }
        }
    }

    @Test
    public void doS() {
        com.google.caliper.runner.CaliperMain.main(Ben.class, new String[0]);

    }
}
