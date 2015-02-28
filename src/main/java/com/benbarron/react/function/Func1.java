package com.benbarron.react.function;

@FunctionalInterface
public interface Func1<S, T> {

    T run(S item) throws Exception;
}
