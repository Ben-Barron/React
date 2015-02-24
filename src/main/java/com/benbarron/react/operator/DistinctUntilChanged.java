package com.benbarron.react.operator;

import com.benbarron.react.Observer;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

public class DistinctUntilChanged<T> implements BiConsumer<T, Observer<T>> {

    private final AtomicReference<T> lastItem = new AtomicReference<>(null);
    private final AtomicBoolean isFirstItem = new AtomicBoolean(false);

    @Override
    public void accept(T item, Observer<T> observer) {
        if (!lastItem.getAndSet(item).equals(item) || isFirstItem.compareAndSet(false, true)) {
            observer.onNext(item);
        }
    }
}
