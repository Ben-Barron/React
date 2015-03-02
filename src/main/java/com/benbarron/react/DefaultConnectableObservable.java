package com.benbarron.react;

import com.benbarron.react.lang.Closeable;
import com.benbarron.react.lang.ImmutableList;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

class DefaultConnectableObservable<O> implements ConnectableObservable<O> {

    private final ImmutableList<Observable<O>> previous;
    private final AtomicReference<ImmutableList<Observer<O>>> next = new AtomicReference<>(ImmutableList.empty());

    DefaultConnectableObservable(ImmutableList<Observable<O>> previous) {
        this.previous = previous;
    }

    @Override
    public Closeable connect() {
        AtomicBoolean isConnected = new AtomicBoolean(true);
        return () -> {
            if (isConnected.compareAndSet(true, false)) {
                // TODO: close here
            }
        };
    }

    @Override
    public Closeable subscribe(Observer<O> observer) {
        next.getAndUpdate(previous -> previous.add(observer));
        return () -> next.getAndUpdate(previous -> previous.remove(observer));
    }
}
