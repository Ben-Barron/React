package com.benbarron.react.function;

@FunctionalInterface
public interface Predicate<T> {

    boolean test(T item) throws Exception;
}
