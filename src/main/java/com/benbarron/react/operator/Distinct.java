package com.benbarron.react.operator;

import com.benbarron.react.Observer;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class Distinct<T> implements BiConsumer<T, Observer<T>> {

    private final Set<T> previousItems = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public void accept(T item, Observer<T> observer) {
        if (previousItems.add(item)) {
            observer.onNext(item);
        }
    }
}
