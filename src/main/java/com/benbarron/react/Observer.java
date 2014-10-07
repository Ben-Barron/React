package com.benbarron.react;

public interface Observer<T> {

    void onComplete();
    void onError(Throwable throwable);
    void onNext(T item);
}
