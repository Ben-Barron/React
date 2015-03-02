package com.benbarron.react;

import com.benbarron.react.internal.exception.IgnoredException;
import com.benbarron.react.internal.exception.ReactException;
import com.benbarron.react.internal.exception.ResubscribeException;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

class DefaultObserver<T> implements Observer<T> {

    private final AtomicInteger closedCount = new AtomicInteger(0);
    private final AtomicBoolean isStopped = new AtomicBoolean(false);
    private final Observer<T> next;
    private final int previousCount;
    private final Runnable onResubscribe;
    private final Runnable onClose;

    DefaultObserver(Observer<T> next,
                    int previousCount,
                    Runnable onResubscribe,
                    Runnable onClose) {

        this.next = next;
        this.previousCount = previousCount;
        this.onResubscribe = onResubscribe;
        this.onClose = onClose;
    }

    public boolean isStopped() {
        return isStopped.get();
    }

    @Override
    public void onComplete() {
        if (closedCount.incrementAndGet() == previousCount && isStopped.compareAndSet(false, true)) {
            try {
                next.onComplete();
            } finally {
                onClose.run();
            }
        }
    }

    @Override
    public void onError(Throwable throwable) {
        if (isStopped.compareAndSet(false, true)) {
            try {
                next.onError(throwable);
            } finally {
                onClose.run();
            }
        }
    }

    @Override
    public void onNext(T item) {
        if (!isStopped.get()) {
            try {
                next.onNext(item);
            } catch (IgnoredException e) {
                throw new ReactException(e.getCause());
            } catch (ResubscribeException e) {
                try {
                    onResubscribe.run();
                } finally {
                    onClose.run();
                }
            } catch (Exception e) {
                onError(e);
            }
        }
    }
}
