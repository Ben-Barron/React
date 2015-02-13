package com.benbarron.react.function;

@FunctionalInterface
public interface Action1<T> {

    void run(T item) throws Throwable;
}
