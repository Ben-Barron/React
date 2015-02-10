package com.benbarron.react;

import com.benbarron.react.lang.ImmutableList;
import org.junit.Test;

public class Example {

    @Test
    public void test() {
        Observable<Number> os = Observable.generate(o -> {
            o.onNext(0);
            o.onNext(0.0f);
            o.onNext(0.0d);

            System.out.println("done");
        });

        os.publish().connect();

        Integer[] items = new Integer[] { 1, 2, 3, 4, 5, 6, 7 };
        ImmutableList<Integer> list = ImmutableList.from(items);

        for (Integer i : list) {
            System.out.println(i);
        }
    }
}
