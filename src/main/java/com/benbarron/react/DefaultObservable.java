package com.benbarron.react;

import com.benbarron.react.lang.Closeable;
import com.benbarron.react.lang.ImmutableList;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;

class DefaultObservable<I, O> implements Observable<O> {

    private final ImmutableList<Observable<I>> previousObservables;
    private final BiFunction<Iterable<Observable<I>>, Observer<O>, Closeable> subscribeAction;
    private final Function<Observer<O>, Observer<I>> observeAction;

    DefaultObservable(ImmutableList<Observable<I>> previousObservables,
                      BiFunction<Iterable<Observable<I>>, Observer<O>, Closeable> subscribeAction,
                      Function<Observer<O>, Observer<I>> observeAction) {

        this.previousObservables = previousObservables;
        this.subscribeAction = subscribeAction;
        this.observeAction = observeAction;
    }

    @Override
    public Closeable subscribe(Observer<O> observer) {
        if (observeAction == null) {
            return subscribeAction.apply(previousObservables, observer);
        }

        AtomicReference<Closeable> closeableRef = new AtomicReference<>(Closeable.empty());
        Observer<I> observerWrapper = new Observer<I>() {

            private final Observer<I> nextObserver = observeAction.apply(observer);
            private final AtomicBoolean isClosed = new AtomicBoolean(false);
            private final AtomicInteger previousClosedCount = new AtomicInteger(0);
            private final int previousCount = previousObservables.size();

            @Override
            public void onComplete() {
                if (previousClosedCount.incrementAndGet() == previousCount && isClosed.compareAndSet(false, true)) {
                    nextObserver.onComplete();
                    closeableRef.getAndSet(Closeable.empty()).close();
                }
            }

            @Override
            public void onError(Throwable throwable) {
                if (isClosed.compareAndSet(false, true)) {
                    nextObserver.onError(throwable);
                    closeableRef.getAndSet(Closeable.empty()).close();
                }
            }

            @Override
            public void onNext(I item) {
                if (!isClosed.get()) {
                    try {
                        nextObserver.onNext(item);
                    } catch (Throwable t) {
                        onError(t);
                    }
                }
            }
        };

        Closeable[] closeablesArray = previousObservables.stream()
                .map(o -> o.subscribe(observerWrapper))
                .toArray(Closeable[]::new);
        ImmutableList<Closeable> closeables = ImmutableList.from(closeablesArray);
        Closeable wrappedClosable = Closeable.from(closeables);

        closeableRef.set(wrappedClosable);

        return wrappedClosable;
    }
}
