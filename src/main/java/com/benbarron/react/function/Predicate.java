package com.benbarron.react.function;

@FunctionalInterface
public interface Predicate<T> {

    boolean run(T item) throws Throwable;
}
