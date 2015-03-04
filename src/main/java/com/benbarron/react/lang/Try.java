package com.benbarron.react.lang;

import com.benbarron.react.function.Action;
import com.benbarron.react.function.Action1;
import com.benbarron.react.function.Func;
import com.benbarron.react.internal.exception.IgnoredException;

import java.util.LinkedList;

public interface Try {

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
