package com.benbarron.react;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;

class DefaultObservable<I, O> implements Observable<O> {

    private final ImmutableList<Observable<I>> previousObservables;
    private final Function<Observer<O>, Observer<I>> observeAction;
    private final BiFunction<ImmutableList<Observable<I>>, Observer<O>, Closeable> subscribeAction;

    DefaultObservable(ImmutableList<Observable<I>> previousObservables,
                      BiFunction<ImmutableList<Observable<I>>, Observer<O>, Closeable> subscribeAction,
                      Function<Observer<O>, Observer<I>> observeAction) {

        this.observeAction = observeAction;
        this.subscribeAction = subscribeAction;
        this.previousObservables = previousObservables;
    }

    @Override
    public Closeable subscribe(Observer<O> observer) {
        if (observeAction != null) {
            AtomicReference<Closeable> closeableRef = new AtomicReference<>(Closeable.empty()); // TODO: race condition here?
            Observer<I> observerWrapper = new Observer<I>() {

                private final Observer<I> nextObserver = observeAction.apply(observer);
                private final AtomicBoolean isClosed = new AtomicBoolean(false);
                private final AtomicInteger previousClosedCount = new AtomicInteger(0);
                private final int previousCount = previousObservables.size();

                @Override
                public void onComplete() {
                    if (previousClosedCount.incrementAndGet() == previousCount && isClosed.compareAndSet(false, true)) {
                        nextObserver.onComplete();
                        closeableRef.get().close();
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    if (isClosed.compareAndSet(false, true)) {
                        nextObserver.onError(throwable);
                        closeableRef.get().close();
                    }
                }

                @Override
                public void onNext(I item) {
                    if (!isClosed.get()) {
                        nextObserver.onNext(item);
                    }
                }
            };

            LinkedList<Closeable> closeables = new LinkedList<>();
            previousObservables.forEach(o -> closeables.add(o.subscribe(observerWrapper)));

            Closeable closeable = Closeable.from(closeables);
            closeableRef.set(closeable);

            return closeable;
        }

        return subscribeAction.apply(previousObservables, observer);
    }
}
