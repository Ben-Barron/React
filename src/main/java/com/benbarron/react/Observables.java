package com.benbarron.react;

import java.util.function.Consumer;
import java.util.function.Function;

public class Observables {

    static <T> Observable<T> generate(Consumer<Observer<T>> observer) {
        return new DefaultObservable<>(
                null,
                (co, o) -> {
                    observer.accept(o);
                    return Closeable.empty();
                },
                null);
    }

    static <T> Observable<T> generate(Function<Observer<T>, Closeable> observer) {
        return new DefaultObservable<>(null, (co, o) -> observer.apply(o), null);
    }
}
