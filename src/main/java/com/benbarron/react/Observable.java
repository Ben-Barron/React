package com.benbarron.react;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;

public interface Observable<T> {

    default Observable<T> distinct() {
        Set<T> items = Collections.newSetFromMap(new ConcurrentHashMap<>());

        return observeAction((T item, Observer<T> observer) -> {
            if (items.add(item)) {
                observer.onNext(item);
            }
        });
    }

    default Observable<T> distinctUntilChanged() {
        AtomicReference<T> last = new AtomicReference<>(null);

        return observeAction((T item, Observer<T> observer) -> {
            if (!last.getAndSet(item).equals(item)) {
                observer.onNext(item);
            }
        });
    }

    default Optional<T> first() {
        return Optional.ofNullable(firstOrDefault());
    }

    default T firstOrDefault() {
        AtomicReference<T> first = new AtomicReference<>(null);
        CountDownLatch latch = new CountDownLatch(1);

        subscribe(i -> {
            first.set(i);
            latch.countDown();
        });

        try {
            latch.await();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return first.get();
    }

    default Observable<T> filter(Predicate<T> predicate) {
        return observeAction((T item, Observer<T> observer) -> {
            if (predicate.test(item)) {
                observer.onNext(item);
            }
        });
    }

    default Observable<T> ignoreElements() {
        return observeAction((T item, Observer<T> observer) -> { });
    }

    default Optional<T> last() {
        return Optional.ofNullable(lastOrDefault());
    }

    default T lastOrDefault() {
        AtomicReference<T> last = new AtomicReference<>(null);
        CountDownLatch latch = new CountDownLatch(1);

        subscribe(i -> {
            last.set(i);
            latch.countDown();
        });

        try {
            latch.await();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return last.get();
    }

    default Observable<T> limit(long limit) {
        AtomicLong count = new AtomicLong(0);

        return observeAction((T item, Observer<T> observer) -> {
            if (count.incrementAndGet() > limit) {
                observer.onComplete();
            } else {
                observer.onNext(item);
            }
        });
    }

    default <R> Observable<R> map(Function<T, R> transform) {
        return observeAction((T item, Observer<R> observer) ->
            observer.onNext(transform.apply(item))
        );
    }

    default <R> Observable<R> mapMany(Function<T, Iterable<R>> transform) {
        return observeAction((T item, Observer<R> observer) ->
            transform.apply(item).forEach(observer::onNext)
        );
    }

    default <R> Observable<R> observeAction(BiConsumer<T, Observer<R>> action) {
        return observeAction(o -> {
            return new Observer<T>() {
                @Override
                public void onComplete() {
                    o.onComplete();
                }

                @Override
                public void onError(Throwable throwable) {
                    o.onError(throwable);
                }

                @Override
                public void onNext(T item) {
                    action.accept(item, o);
                }
            };
        });
    }

    <R> Observable<R> observeAction(Function<Observer<R>, Observer<T>> action);

    default Observable<T> observeOn(ExecutorService executorService) {
        return observeAction((T item, Observer<T> observer) ->
            executorService.submit(() -> observer.onNext(item))
        );
    }

    ConnectableObservable<T> publish();

    default Observable<T> skip(long skip) {
        AtomicLong count = new AtomicLong(0);

        return observeAction((T item, Observer<T> observer) -> {
            if (count.incrementAndGet() > skip) {
                observer.onNext(item);
            }
        });
    }

    default Closeable subscribe() {
        return subscribe(i -> {});
    }

    default Closeable subscribe(Consumer<T> onNext) {
        return subscribe(onNext, () -> {});
    }

    default Closeable subscribe(Consumer<T> onNext, Runnable onComplete) {
        return subscribe(onNext, onComplete, t -> { throw new RuntimeException(t); });
    }

    default Closeable subscribe(Consumer<T> onNext, Runnable onComplete, Consumer<Throwable> onError) {
        return subscribe(new Observer<T>() {
            @Override
            public void onComplete() {
                onComplete.run();
            }

            @Override
            public void onError(Throwable throwable) {
                onError.accept(throwable);
            }

            @Override
            public void onNext(T item) {
                onNext.accept(item);
            }
        });
    }

    Closeable subscribe(Observer<T> observer);

    Observable<T> subscribeAction(BiFunction<Collection<Observable<T>>, Observer<T>, Closeable> action);

    default Observable<T> take(long number) {
        AtomicLong count = new AtomicLong(0);

        return observeAction((T item, Observer<T> observer) -> {
            long c = count.incrementAndGet();

            if (c < number) {
                observer.onNext(item);
            } else if (c == number) {
                observer.onComplete();
            }
        });
    }

}
