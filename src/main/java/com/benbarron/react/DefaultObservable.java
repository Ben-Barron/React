package com.benbarron.react;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

class DefaultObservable<I, O> implements Observer<I>, Observable<O> {

    private final Set<Observable<I>> previous = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<AutoCloseable> closables = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private final BiConsumer<I, Observer<O>> action;
	private final AtomicBoolean isSubscribed = new AtomicBoolean(false);
    private final AtomicBoolean isCompleted = new AtomicBoolean(false);
    private final AtomicBoolean isErrored = new AtomicBoolean(false);
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    private volatile ExecutorService subscribeOn;
	private volatile Observer<O> next;

    DefaultObservable(BiConsumer<I, Observer<O>> action, Observable<I> previous) {
        this.action = action;

        if (previous != null) {
            this.previous.add(previous);
        }
    }

	@Override
	public <R> Observable<R> action(BiConsumer<O, Observer<R>> action) {
		return new DefaultObservable<>(action, this);
	}
	
	@Override
	public void onComplete() {
        if (isCompleted.compareAndSet(false, true)) {
            next.onComplete();
        }
	}

	@Override
	public void onError(Throwable throwable) {
        if (isErrored.compareAndSet(false, true)) {
            next.onError(throwable);
        }
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onNext(I item) {
        if (isCompleted.get() || isClosed.get() || isErrored.get()) {
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
        DefaultObservable<O, O> newObservable = new DefaultObservable<>(null, this);
        newObservable.previous.add(observable);

        return newObservable;
    }

    @Override
    public AutoCloseable subscribe(Observer<O> observer) {
        DefaultObservable<I, O> observable;

        if (isSubscribed.compareAndSet(false, true)) {
            observable = this;
        } else {
            observable = new DefaultObservable<>(action, null);
            observable.previous.addAll(previous);
        }

        observable.next = observer;

        if (subscribeOn != null) {
            subscribeOn.submit(() -> {
                if (!observable.previous.isEmpty()) {
                    observable.previous.forEach(i -> observable.closables.add(i.subscribe(observable)));
                } else {
                    action.accept(null, next);
                }
            });
        } else {
            if (!observable.previous.isEmpty()) {
                observable.previous.forEach(i -> observable.closables.add(i.subscribe(observable)));
            } else {
                action.accept(null, next);
            }
        }

        return () -> {
            if (isClosed.compareAndSet(false, true)) {
                for (AutoCloseable closable : closables) {
                    try {
                        closable.close();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };
    }

    @Override
    public Observable<O> subscribeOn(ExecutorService executorService) {
        subscribeOn = executorService;
        return this;
    }
}
