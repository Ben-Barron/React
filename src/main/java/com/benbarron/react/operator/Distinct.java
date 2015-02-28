package com.benbarron.react.operator;

import com.benbarron.react.Observer;
import com.benbarron.react.function.Action2;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Distinct<T> implements Action2<T, Observer<T>> {

    private final Set<T> previousItems = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public void run(T item, Observer<T> observer) {
        if (previousItems.add(item)) {
            observer.onNext(item);
        }
    }
}
