package com.benbarron.react;

import com.benbarron.react.function.Func1;
import com.benbarron.react.lang.*;

import java.util.concurrent.atomic.AtomicReference;

class OnObserveObservable<I, O> implements Observable<O> {

    private final ImmutableList<Observable<I>> previous;
    private final Func1<Observer<O>, Observer<I>> onObserve;

    OnObserveObservable(ImmutableList<Observable<I>> previous,
                        Func1<Observer<O>, Observer<I>> onObserve) {

        this.previous = previous;
        this.onObserve = onObserve;
    }

    @Override
    public Closeable subscribe(Observer<O> observer) {
        AtomicReference<Closeable> closeableRef = new AtomicReference<>(Closeable.empty());
        DefaultObserver<I> wo = new DefaultObserver<>(
                Try.get(() -> onObserve.run(observer)),
                previous.size(),
                () -> closeableRef.getAndSet(Closeable.empty()).close(),
                () -> subscribe(observer));

        for (Observable<I> observable : previous) {
            if (wo.isStopped()) {
                break;
            }

            //closeables = closeables.add();
            //closeableRef.set(Closeable.from(closeables));
        }

        return closeableRef.get();
    }
}
