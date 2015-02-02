package com.benbarron.react;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

class DefaultObservable<I, O> extends AbstractObservable<O> implements Observable<O> {

    private final Collection<Observable<I>> previous;
    private final Function<Observer<O>, Observer<I>> observeAction;
    private final BiFunction<Collection<Observable<I>>, Observer<O>, Closeable> subscribeAction;

    DefaultObservable(Function<Observer<O>, Observer<I>> observeAction,
                      BiFunction<Collection<Observable<I>>, Observer<O>, Closeable> subscribeAction,
                      Collection<Observable<I>> previous) {

        this.observeAction = observeAction;
        this.subscribeAction = subscribeAction;
        this.previous = previous;
    }

    @Override
    public Closeable subscribe(Observer<O> observer) {
        if (observeAction != null) {
            Observer<I> observerWrapper = new Observer<I>() {

                private final Observer<I> next = observeAction.apply(observer);
                private final AtomicBoolean isClosed = new AtomicBoolean(false);
                private final AtomicInteger previousClosedCount = new AtomicInteger(0);
                private final int previousCount = previous.size();

                @Override
                public void onComplete() {
                    if (previousClosedCount.incrementAndGet() == previousCount && isClosed.compareAndSet(false, true)) {
                        next.onComplete();
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    if (isClosed.compareAndSet(false, true)) {
                        next.onError(throwable);
                    }
                }

                @Override
                public void onNext(I item) {
                    if (!isClosed.get()) {
                        next.onNext(item);
                    }
                }
            };

            return Closeable.from(previous.stream().map(p -> p.subscribe(observerWrapper)).collect(Collectors.toList()));
        }

        return subscribeAction.apply(previous, observer);
    }
}
