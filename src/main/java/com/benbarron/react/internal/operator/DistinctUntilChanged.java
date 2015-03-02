package com.benbarron.react.internal.operator;

import com.benbarron.react.Observer;
import com.benbarron.react.function.Action2;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class DistinctUntilChanged<T> implements Action2<T, Observer<T>> {

    private final AtomicReference<T> lastItem = new AtomicReference<>(null);
    private final AtomicBoolean isFirstItem = new AtomicBoolean(false);

    @Override
    public void run(T item, Observer<T> observer) {
        if (!lastItem.getAndSet(item).equals(item) || isFirstItem.compareAndSet(false, true)) {
            observer.onNext(item);
        }
    }
}
