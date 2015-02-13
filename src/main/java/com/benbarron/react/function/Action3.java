package com.benbarron.react.function;

@FunctionalInterface
public interface Action3<T, S, U> {

    void run(T item1, S item2, U item3) throws Throwable;
}
