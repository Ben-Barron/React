package com.benbarron.react;

public interface ConnectableObservable<T> extends Observable<T> {

    Closeable connect();
}
