package com.benbarron.react;

import com.benbarron.react.lang.Closeable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

class DefaultConnectableObservable<O> implements ConnectableObservable<O> {

    private final Collection<Observable<O>> previous;
    private final Collection<Observer<O>> next = new ConcurrentLinkedQueue<>();

    DefaultConnectableObservable(Collection<Observable<O>> previous) {
        this.previous = previous;
    }

    @Override
    public Closeable connect() {
        return Closeable.empty();
    }

    @Override
    public Closeable subscribe(Observer<O> observer) {
        return Closeable.empty();
    }
}
