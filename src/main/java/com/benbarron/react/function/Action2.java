package com.benbarron.react.function;

@FunctionalInterface
public interface Action2<T, S> {

    void run(T item1, S item2) throws Throwable;
}
