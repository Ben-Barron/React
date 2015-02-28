package com.benbarron.react;

import com.benbarron.react.function.Func1;
import com.benbarron.react.function.Func2;
import com.benbarron.react.lang.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

class DefaultObservable<I, O> implements Observable<O> {

    private final ImmutableList<Observable<I>> previous;
    private final Func2<Iterable<Observable<I>>, Observer<O>, Closeable> onSubscribe;
    private final Func1<Observer<O>, Observer<I>> onObserve;

    DefaultObservable(ImmutableList<Observable<I>> previous,
                      Func2<Iterable<Observable<I>>, Observer<O>, Closeable> onSubscribe,
                      Func1<Observer<O>, Observer<I>> onObserve) {

        this.previous = previous;
        this.onSubscribe = onSubscribe;
        this.onObserve = onObserve;
    }

    @Override
    public Closeable subscribe(Observer<O> observer) {
        if (onObserve == null) {
            return Try.get(() -> onSubscribe.run(previous, observer));
        }

        AtomicBoolean isClosed = new AtomicBoolean(false);
        AtomicReference<Closeable> closeableRef = new AtomicReference<>(Closeable.empty());
        Observer<I> observerWrapper = new Observer<I>() {

            private final AtomicInteger previousClosedCount = new AtomicInteger(0);
            private final Observer<I> nextObserver = Try.get(() -> onObserve.run(observer));

            @Override
            public void onComplete() {
                if (previousClosedCount.incrementAndGet() == previous.size() && isClosed.compareAndSet(false, true)) {
                    Try.run(nextObserver::onComplete);
                    closeableRef.getAndSet(Closeable.empty()).close();
                }
            }

            @Override
            public void onError(Throwable throwable) {
                if (isClosed.compareAndSet(false, true)) {
                    Try.run(() -> nextObserver.onError(throwable));
                    closeableRef.getAndSet(Closeable.empty()).close();
                }
            }

            @Override
            public void onNext(I item) {
                if (!isClosed.get()) {
                    try {
                        nextObserver.onNext(item);
                    } catch (IgnoredException e) {
                        throw new RuntimeException(e.getCause());
                    } catch (ResubscribeException e) {
                        subscribe(observer);
                    } catch (Exception e) {
                        onError(e);
                    }
                }
            }
        };

        ImmutableList<Closeable> closeables = ImmutableList.empty();

        for (Observable<I> observable : previous) {
            if (isClosed.get()) {
                closeableRef.getAndSet(Closeable.empty()).close(); // TODO: this right?
                break;
            }

            closeables = closeables.add(observable.subscribe(observerWrapper));
            closeableRef.set(Closeable.from(closeables));
        }

        return closeableRef.get();
    }
}
