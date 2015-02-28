package com.benbarron.react.lang;

import com.benbarron.react.function.Action;
import com.benbarron.react.function.Func;

public interface Try {

    static <T> T get(Func<T> producer) {
        try {
            return producer.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void ignore(Action action) {
        try {
            action.run();
        } catch (Exception e) {
            throw new IgnoredException(e);
        }
    }

    static void run(Action action) {
        try {
            action.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
