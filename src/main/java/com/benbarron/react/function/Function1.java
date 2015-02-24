package com.benbarron.react.function;

@FunctionalInterface
public interface Function1<S, T> {

    T run(S item) throws Exception;
}
