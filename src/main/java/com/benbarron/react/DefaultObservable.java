package com.benbarron.react;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

class DefaultObservable<I, O> implements Observable<O> {

    private final Collection<Observable<I>> previousObservables;
    private final Function<Observer<O>, Observer<I>> observeAction;
    private final BiFunction<Collection<Observable<I>>, Observer<O>, Closeable> subscribeAction;

    DefaultObservable(Collection<Observable<I>> previousObservables,
                      BiFunction<Collection<Observable<I>>, Observer<O>, Closeable> subscribeAction,
                      Function<Observer<O>, Observer<I>> observeAction) {

        this.observeAction = observeAction;
        this.subscribeAction = subscribeAction;
        this.previousObservables = previousObservables;
    }

    @Override
    public Closeable subscribe(Observer<O> observer) {
        if (observeAction != null) {
            Observer<I> observerWrapper = new Observer<I>() {

                private final Observer<I> nextObserver = observeAction.apply(observer);
                private final AtomicBoolean isClosed = new AtomicBoolean(false);
                private final AtomicInteger previousClosedCount = new AtomicInteger(0);
                private final int previousCount = previousObservables.size();

                @Override
                public void onComplete() {
                    if (previousClosedCount.incrementAndGet() == previousCount && isClosed.compareAndSet(false, true)) {
                        nextObserver.onComplete();
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    if (isClosed.compareAndSet(false, true)) {
                        nextObserver.onError(throwable);
                    }
                }

                @Override
                public void onNext(I item) {
                    if (!isClosed.get()) {
                        nextObserver.onNext(item);
                    }
                }
            };

            return Closeable.from(previousObservables.stream().map(p -> p.subscribe(observerWrapper)).collect(Collectors.toCollection(LinkedList::new)));
        }

        return subscribeAction.apply(previousObservables, observer);
    }
}
