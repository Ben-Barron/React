package com.benbarron.react.lang;

import com.benbarron.react.function.Action;
import com.benbarron.react.function.Action1;
import com.benbarron.react.function.Func;
import com.benbarron.react.internal.exception.IgnoredException;

import java.util.LinkedList;

public interface Try {

    static <T> void forEach(Iterable<T> iterable, Action1<T> action) {
        LinkedList<Exception> exceptions = new LinkedList<>();

        iterable.forEach(i -> {
            try {
                action.run(i);
            } catch (Exception e) {
                exceptions.add(e);
            }
        });

        if (!exceptions.isEmpty()) {
            // TODO: throw a combined excetpion;
        }
    }

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
