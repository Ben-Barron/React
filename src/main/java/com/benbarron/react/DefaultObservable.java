package com.benbarron.react;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

class DefaultObservable<I, O> implements Observer<I>, Observable<O> {

    protected final BiConsumer<I, Observer<O>> action;
    protected final Set<Observable<I>> previous = Collections.newSetFromMap(new ConcurrentHashMap<>());
    protected final Set<AutoCloseable> closables = new HashSet<>();
    protected final AtomicBoolean isSubscribed = new AtomicBoolean(false);
    protected final AtomicBoolean isClosed = new AtomicBoolean(false);
    protected final AtomicInteger completeCount = new AtomicInteger(0);

    protected volatile Observer<O> next;
    protected volatile ExecutorService subscribeOn;

    @SafeVarargs
    DefaultObservable(BiConsumer<I, Observer<O>> action, Observable<I> ... previous) {
        this.action = action;
        Collections.addAll(this.previous, previous);
    }

    @Override
    public <R> Observable<R> action(BiConsumer<O, Observer<R>> action) {
        return new DefaultObservable<>(action, this);
    }

    @Override
    public void onComplete() {
        if (completeCount.incrementAndGet() == previous.size()) {
            runThenClose(next::onComplete);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        runThenClose(() -> next.onError(throwable));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onNext(I item) {
        if (isClosed.get()) {
            return;
        }

        try {
            if (action != null) {
                action.accept(item, next);
            } else {
                next.onNext((O) item);
            }
        } catch (Exception e) {
            onError(e);
        }
    }

    @Override
    public Observable<O> merge(Observable<O> observable) {
        return new DefaultObservable<>(null, this, observable);
    }

    @Override
    public ConnectableObservable<O> publish() {
        return null; // TODO write
    }

    @Override
    public AutoCloseable subscribe(Observer<O> observer) {
        DefaultObservable<I, O> observable;

        if (isSubscribed.compareAndSet(false, true)) {
            observable = this;
        } else {
            observable = new DefaultObservable<>(action);
            observable.previous.addAll(previous);
        }

        observable.next = observer;

        if (subscribeOn != null) {
            subscribeOn.submit(() -> internalSubscribe(observable));
        } else {
            internalSubscribe(observable);
        }

        return () -> runThenClose(null);
    }

    @Override
    public Observable<O> subscribeOn(ExecutorService executorService) {
        subscribeOn = executorService;
        return this;
    }

    private void internalSubscribe(DefaultObservable<I, O> observable) {
        if (!observable.previous.isEmpty()) {
            observable.previous.forEach(i -> observable.closables.add(i.subscribe(observable)));
        } else {
            observable.action.accept(null, next);
        }
    }

    private void runThenClose(Runnable runnable) {
        if (isClosed.compareAndSet(false, true)) {
            if (runnable != null) {
                runnable.run();
            }

            for (AutoCloseable closable : closables) {
                try {
                    closable.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
