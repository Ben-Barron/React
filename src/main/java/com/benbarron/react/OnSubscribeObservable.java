package com.benbarron.react;

import com.benbarron.react.function.Func2;
import com.benbarron.react.internal.exception.OnSubscribeException;
import com.benbarron.react.lang.Closeable;
import com.benbarron.react.lang.ImmutableList;

class OnSubscribeObservable<I, O> implements Observable<O> {

    private final ImmutableList<Observable<I>> previous;
    private final Func2<Iterable<Observable<I>>, Observer<O>, Closeable> onSubscribe;

    OnSubscribeObservable(ImmutableList<Observable<I>> previous,
                          Func2<Iterable<Observable<I>>, Observer<O>, Closeable> onSubscribe) {

        this.previous = previous;
        this.onSubscribe = onSubscribe;
    }

    @Override
    public Closeable subscribe(Observer<O> observer) {
        try {
            return onSubscribe.run(previous, observer);
        } catch (Exception e) {
            throw new OnSubscribeException(e);
        }
    }
}
