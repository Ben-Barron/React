package com.benbarron.react;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class BehaviorSubject<T> extends AbstractObservable<T> implements Closeable, Observable<T>, Observer<T> {

    private final AtomicBoolean isClosed = new AtomicBoolean(true);
    private final Collection<Observer<T>> next = new ConcurrentLinkedQueue<>();

    private volatile T initialValue;

    public BehaviorSubject(T initialValue) {
        this.initialValue = initialValue;
    }

    @Override
    public void close() {
        if (isClosed.compareAndSet(false, true)) {
            next.clear();
            initialValue = null;
        }
    }

    @Override
    public void onComplete() {
        if (isClosed.compareAndSet(false, true)) {
            next.forEach(Observer::onComplete);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        if (isClosed.compareAndSet(false, true)) {
            next.forEach(o -> o.onError(throwable));
        }
    }

    @Override
    public void onNext(T item) {
        if (!isClosed.get()) {
            next.forEach(o -> o.onNext(item));
        }
    }

    @Override
    public Closeable subscribe(Observer<T> observer) {
        next.add(observer);
        observer.onNext(initialValue);

        return () -> next.remove(observer);
    }
}
