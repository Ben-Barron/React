package com.benbarron.react.subject;

import com.benbarron.react.Closeable;
import com.benbarron.react.Observable;
import com.benbarron.react.Observer;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class ReplaySubject<T> implements Closeable, Observable<T>, Observer<T> {

    private final Collection<Observer<T>> next = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isClosed = new AtomicBoolean(true);
    private final ConcurrentLinkedDeque<T> replayQueue = new ConcurrentLinkedDeque<>();
    private final AtomicLong size = new AtomicLong(0);
    private final long count;

    public ReplaySubject(long count) {
        this.count = count;
    }

    @Override
    public void close() {
        if (isClosed.compareAndSet(false, true)) {
            next.clear();
            replayQueue.clear();
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
            long initialSize = size.get();

            if (initialSize < count) {
            } else {
                replayQueue.removeFirst();
                long nextSize = initialSize + 1l;
            }

            next.forEach(o -> o.onNext(item));
        }
    }

    @Override
    public Closeable subscribe(Observer<T> observer) {
        next.add(observer);
        replayQueue.forEach(observer::onNext);

        return () -> next.remove(observer);
    }
}
