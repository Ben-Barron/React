package com.benbarron.react;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface Observable<T> {

    <R> Observable<R> observeAction(Function<Observer<R>, Observer<T>> action);

    ConnectableObservable<T> publish();

    Closeable subscribe(Observer<T> observer);

    Observable<T> subscribeAction(BiFunction<Collection<Observable<T>>, Observer<T>, Closeable> action);
}
