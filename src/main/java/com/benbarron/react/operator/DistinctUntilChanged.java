package com.benbarron.react.operator;

import com.benbarron.react.Observer;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

// TODO: what if first item is null?
public class DistinctUntilChanged<T> implements BiConsumer<T, Observer<T>> {

    private final AtomicReference<T> lastItem = new AtomicReference<>(null);

    @Override
    public void accept(T item, Observer<T> observer) {
        if (!lastItem.getAndSet(item).equals(item)) {
            observer.onNext(item);
        }
    }
}
