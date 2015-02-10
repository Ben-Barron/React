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
        AtomicBoolean isConnected = new AtomicBoolean(true);
        Observer<O> observer = new Observer<O>() {

            private final AtomicInteger previousClosedCount = new AtomicInteger(0);
            private final int previousCount = previous.size();

            @Override
            public void onComplete() {
                if (previousClosedCount.incrementAndGet() == previousCount && isConnected.compareAndSet(true, false)) {
                    next.forEach(Observer::onComplete);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                if (isConnected.compareAndSet(true, false)) {
                    next.forEach(i -> i.onError(throwable));
                }
            }

            @Override
            public void onNext(O item) {
                if (isConnected.get()) {
                    next.forEach(i -> i.onNext(item));
                }
            }
        };
        Closeable closable = Closeable.from(previous.stream().map(p -> p.subscribe(observer)).collect(Collectors.toCollection(LinkedList::new)));

        return () -> {
            if (isConnected.compareAndSet(true, false)) {
                closable.close();
            }
        };
    }

    @Override
    public Closeable subscribe(Observer<O> observer) {
        next.add(observer);
        return () -> next.remove(observer);
    }
}
