package com.benbarron.react;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;

abstract class AbstractObservable<O> implements Observable<O> {

    @Override
    public <R> Observable<R> observeAction(Function<Observer<R>, Observer<O>> action) {
        return new DefaultObservable<>(action, null, Arrays.asList(this));
    }

    @Override
    public ConnectableObservable<O> publish() {
        return new DefaultConnectableObservable<>(Arrays.asList(this));
    }

    @Override
    public Observable<O> subscribeAction(BiFunction<Collection<Observable<O>>, Observer<O>, Closeable> action) {
        return new DefaultObservable<>(null, action, Arrays.asList(this));
    }
}
