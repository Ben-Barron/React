package com.benbarron.react;

import com.benbarron.react.function.Func1;
import com.benbarron.react.internal.exception.ObservableSubscribeException;
import com.benbarron.react.lang.*;

import java.util.concurrent.atomic.AtomicReference;

class ObservingObservable<I, O> implements Observable<O> {

    private final ImmutableList<Observable<I>> previous;
    private final Func1<Observer<O>, Observer<I>> onObserve;

    ObservingObservable(ImmutableList<Observable<I>> previous,
                        Func1<Observer<O>, Observer<I>> onObserve) {

        this.previous = previous;
        this.onObserve = onObserve;
    }

    @Override
    public Closeable subscribe(Observer<O> observer) {
        AtomicReference<ImmutableList<Closeable>> closeableRef = new AtomicReference<>(ImmutableList.empty());
        Observer<I> transformedObserver;

        try {
            transformedObserver = onObserve.run(observer);
        } catch (Exception e) {
            throw new ObservableSubscribeException(e);
        }

        DefaultObserver<I> wrappedObserver = new DefaultObserver<>(
                transformedObserver,
                previous.size(),
                () -> Closeable.from(closeableRef.getAndSet(ImmutableList.empty())).close(),
                () -> subscribe(observer));

        for (Observable<I> observable : previous) {
            if (wrappedObserver.isStopped()) {
                break;
            }

            closeableRef.updateAndGet(list -> list.add(observable.subscribe(wrappedObserver)));
        }

        return Closeable.from(closeableRef.get());
    }
}
