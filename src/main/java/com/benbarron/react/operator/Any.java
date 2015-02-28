package com.benbarron.react.operator;

import com.benbarron.react.Observer;
import com.benbarron.react.function.Func1;
import com.benbarron.react.function.Predicate;
import com.benbarron.react.lang.Try;

public class Any<T> implements Func1<Observer<Boolean>, Observer<T>> {

    private final Predicate<T> predicate;

    public Any(Predicate<T> predicate) {
        this.predicate = predicate;
    }

    @Override
    public Observer<T> run(Observer<Boolean> observer) {
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
                if (Try.get(() -> predicate.test(item))) {
                    observer.onNext(true);
                    observer.onComplete();
                }
            }
        };
    }
}
