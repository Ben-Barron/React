package com.benbarron.react.function;

@FunctionalInterface
public interface Function2<S, U, T> {

    T run(S item1, U item2) throws Throwable;
}
