package com.benbarron.react.operator;

import com.benbarron.react.Observer;

import java.util.function.Function;
import java.util.function.Predicate;

public class Any<T> implements Function<Observer<Boolean>, Observer<T>> {

    private final Predicate<T> predicate;

    public Any(Predicate<T> predicate) {
        this.predicate = predicate;
    }

    @Override
    public Observer<T> apply(Observer<Boolean> observer) {
        return new Observer<T>() {
            @Override
            public void onComplete() {
                observer.onNext(false);
                observer.onComplete();
            }

            @Override
            public void onError(Throwable throwable) {
                observer.onError(throwable);
            }

            @Override
            public void onNext(T item) {
                if (predicate.test(item)) {
                    observer.onNext(true);
                    observer.onComplete();
                }
            }
        };
    }
}
