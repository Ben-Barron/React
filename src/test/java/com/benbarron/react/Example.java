package com.benbarron.react;

import com.benbarron.react.lang.ImmutableList;
import org.junit.Test;

public class Example {

    @Test
    public void test() {
        Observable.<String>generate(
                (o) -> {
                    o.onNext("hey");
                    o.onNext("ho");
                    o.onNext("hey");

                    System.out.println("done");
                })
                .asObservable()
                .distinctUntilChanged()
                .distinct()
                .any((i) ->  { throw new Exception(); })
                .ox((b, o) -> o.onNext("S"));


        //os.publish().connect();

        Integer[] items = new Integer[] { 1, 2, 3, 4, 5, 6, 7 };
        ImmutableList<Integer> list = ImmutableList.from(items);

        for (Integer i : list) {
            System.out.println(i);
        }
    }
}
