package com.benbarron.react.function;

@FunctionalInterface
public interface Func<T> {

    T run() throws Exception;
}
