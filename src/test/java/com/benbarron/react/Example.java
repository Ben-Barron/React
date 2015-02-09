package com.benbarron.react;

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
    }
}
