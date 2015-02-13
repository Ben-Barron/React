package com.benbarron.react.function;

@FunctionalInterface
public interface Action4<T, S, U, V> {

    void run(T item1, S item2, U item3, V item4) throws Throwable;
}
