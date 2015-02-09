package com.benbarron.react.subject;

import com.benbarron.react.Closeable;
import com.benbarron.react.Observable;
import com.benbarron.react.Observer;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Subject<T> implements Closeable, Observable<T>, Observer<T> {

    private final Collection<Observer<T>> next = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isClosed = new AtomicBoolean(true);

    @Override
    public void close() {
        onComplete();
        next.clear();
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
        return () -> next.remove(observer);
    }
}
