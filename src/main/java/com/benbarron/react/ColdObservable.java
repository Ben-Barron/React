package com.benbarron.react;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

class ColdObservable<I, O> implements AutoCloseable, Observer<I>, Obervable2<O> {

    private final Function<Observer<O>, Observer<I>> observeAction;
    private final BiFunction<Collection<Obervable2<O>>, Observer<O>, Collection<AutoCloseable>> subscribeAction;
    private final Collection<AutoCloseable> closeables;
    private final Collection<Obervable2<I>> previous;
    private final AtomicLong previousClosedCount;
    private final AtomicBoolean isClosed;
    private final Observer<I> next;

    ColdObservable(Function<Observer<O>, Observer<I>> observeAction,
                   BiFunction<Collection<Obervable2<O>>, Observer<O>, Collection<AutoCloseable>> subscribeAction,
                   Collection<AutoCloseable> closeables,
                   Collection<Obervable2<I>> previous,
                   Observer<I> next) {

        this.observeAction = observeAction;
        this.subscribeAction = subscribeAction;
        this.closeables = closeables;
        this.previous = previous;
        this.next = next;

        if (next != null) {
            this.previousClosedCount = new AtomicLong(0);
            this.isClosed = new AtomicBoolean(false);
        } else {
            this.previousClosedCount = null;
            this.isClosed = null;
        }
    }

    @Override
    public void close() throws Exception {
        internalClose(null);
    }

    @Override
    public <R> Obervable2<R> observeAction(Function<Observer<R>, Observer<O>> action) {
        return new ColdObservable<>(action, null, null, Arrays.asList(this), null);
    }

    @Override
    public void onComplete() {
        if (previousClosedCount.incrementAndGet() == previous.size()) {
            internalClose(next::onComplete);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        internalClose(() -> next.onError(throwable));
    }

    @Override
    public void onNext(I item) {
        if (!isClosed.get()) {
            next.onNext(item);
        }
    }

    @Override
    public AutoCloseable subscribe(Observer<O> observer) {
        Collection<AutoCloseable> closables = null;

        if (subscribeAction != null) {
            closables = subscribeAction.apply(previous, observer);
        } else if (previous != null) {
            closables = previous.stream()
                                .map(p -> p.subscribe(observer))
                                .collect(Collectors.toList()); // TODO: trim list?
        }

        if (observeAction != null) {
            Observer<I> next = observeAction.apply(observer);
            return new ColdObservable<>(null, null, closables, previous, next);
        }

        return new ColdObservable<>(null, null, closables, previous, observer); // TODO: what about closables?
    }

    @Override
    public Obervable2<O> subscribeAction(BiFunction<Collection<Obervable2<O>>, Observer<O>, Collection<AutoCloseable>> action) {
        return new ColdObservable<>(null, action, null, Arrays.asList(this), null);
    }

    private void internalClose(Runnable runnable) {
        if (isClosed.compareAndSet(false, true)) {
            if (runnable != null) {
                runnable.run();
            }

            if (closeables == null) {
                return;
            }

            for (AutoCloseable closable : closeables) {
                try {
                    closable.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
