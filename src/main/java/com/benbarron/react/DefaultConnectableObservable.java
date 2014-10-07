package com.benbarron.react;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

class DefaultConnectableObservable<I, O> extends DefaultObservable<I, O> implements ConnectableObservable<O> {

    private final Set<Observer<O>> next = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final AtomicBoolean isConnected = new AtomicBoolean(false);

    @SafeVarargs
    DefaultConnectableObservable(Observable<I> ... previous) {
        super(null, previous);
    }

    @Override
    public AutoCloseable connect() {
        if (isConnected.compareAndSet(false, true)) {
            this.previous.forEach(i -> this.closables.add(i.subscribe(this)));
        }

        return () -> {
            if (isConnected.compareAndSet(true, false)) {
                for (AutoCloseable closable : closables) {
                    try {
                        closable.close();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                closables.clear();
            }
        };
    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onNext(I item) {

    }

    @Override
    public ConnectableObservable<O> publish() {
        return this;
    }

    @Override
    public AutoCloseable subscribe(Observer<O> observer) {
        next.add(observer);
        return () -> next.remove(observer);
    }
}
