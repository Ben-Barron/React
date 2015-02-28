package com.benbarron.react.function;

@FunctionalInterface
public interface Func3<S, U, V, T> {

    T run(S item1, U item2, V item3) throws Exception;
}
