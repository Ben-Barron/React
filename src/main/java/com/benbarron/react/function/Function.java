package com.benbarron.react.function;

@FunctionalInterface
public interface Function<T> {

    T run() throws Exception;
}
