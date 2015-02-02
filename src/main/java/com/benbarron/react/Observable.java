package com.benbarron.react;

import javafx.collections.ObservableArray;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;

public interface Observable<T> {

    <R> Observable<R> action(BiConsumer<T, Observer<R>> action);

    default <R> Observable<R> action(Function<Observable<T>, Observable<R>> transformer) {
        return transformer.apply(this);
    }

    default Observable<T> actionOnSubscribe(Consumer<Runnable> subscribeCall) {
        return this;
    }

    default Observable<T> distinct() {
        Set<T> items = Collections.newSetFromMap(new ConcurrentHashMap<>());

        return action((T item, Observer<T> observer) -> {
            if (items.add(item)) {
                observer.onNext(item);
            }
        });
    }

    default Observable<T> distinctUntilChanged() {
        AtomicReference<T> last = new AtomicReference<>(null);

        return action((T item, Observer<T> observer) -> {
            if (!last.getAndSet(item).equals(item)) {
                observer.onNext(item);
            }
        });
    }

    default Optional<T> first() {
        return Optional.ofNullable(firstOrDefault());
    }

    default T firstOrDefault() {
        ArrayBlockingQueue<T> queue = new ArrayBlockingQueue<>(1);

        subscribe(queue::add,() -> queue.add(null), t -> { throw new RuntimeException(t); });

        try {
            return queue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    default Observable<T> filter(Predicate<T> predicate) {
        return action((T item, Observer<T> observer) -> {
            if (predicate.test(item)) {
                observer.onNext(item);
            }
        });
    }

    default Observable<T> ignoreElements() {
        return action((T item, Observer<T> observer) -> { });
    }

    default Optional<T> last() {
        return Optional.ofNullable(lastOrDefault());
    }

    default T lastOrDefault() {
        AtomicReference<T> last = new AtomicReference<>(null);
        Semaphore mutex = new Semaphore(1);
        mutex.acquireUninterruptibly();

        subscribe(last::set, mutex::release, t -> { throw new RuntimeException(t); });

        return last.get();
    }

    default Observable<T> limit(long limit) {
        AtomicLong count = new AtomicLong(0);

        return action((T item, Observer<T> observer) -> {
            if (count.incrementAndGet() > limit) {
                observer.onComplete();
            } else {
                observer.onNext(item);
            }
        });
    }

    default <R> Observable<R> map(Function<T, R> transform) {
        return action((T item, Observer<R> observer) -> {
            observer.onNext(transform.apply(item));
        });
    }

    default <R> Observable<R> mapMany(Function<T, Iterable<R>> transform) {
        return action((T item, Observer<R> observer) -> {
            transform.apply(item).forEach(observer::onNext);
        });
    }

    Observable<T> merge(Observable<T> observable);

    default Observable<T> observeOn(ExecutorService executorService) {
        return action((T item, Observer<T> observer) -> {
            executorService.submit(() -> observer.onNext(item));
        });
    }

    ConnectableObservable<T> publish();

    default Observable<T> run(Consumer<T> consumer) {
        return action((T item, Observer<T> observer) -> {
            consumer.accept(item);
            observer.onNext(item);
        });
    }

    default Observable<T> skip(long skip) {
        AtomicLong count = new AtomicLong(0);

        return action((T item, Observer<T> observer) -> {
            if (count.incrementAndGet() > skip) {
                observer.onNext(item);
            }
        });
    }

    default AutoCloseable subscribe() {
        return subscribe(i -> {}, () -> {}, t -> {});
    }

    default AutoCloseable subscribe(Consumer<T> onNext) {
        return subscribe(onNext, () -> {}, t -> {});
    }

    default AutoCloseable subscribe(Consumer<T> onNext, Runnable onComplete) {
        return subscribe(onNext, onComplete, t -> {});
    }

    default AutoCloseable subscribe(Consumer<T> onNext, Runnable onComplete, Consumer<Throwable> onError) {
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
    
    AutoCloseable subscribe(Observer<T> observer);


    default Observable<T> take(long number) {
        AtomicLong count = new AtomicLong(0);

        return action((T item, Observer<T> observer) -> {
            long c = count.incrementAndGet();

            if (c < number) {
                observer.onNext(item);
            } else if (c == number) {
                observer.onComplete();
            }
        });
    }

    static <T> Observable<T> generate(Consumer<Observer<T>> observer) {
        return new DefaultObservable<>((T item, Observer<T> o) -> observer.accept(o));
    }

    static <T> Observable<T> generate(Function<Consumer<Observer<T>>, AutoCloseable> observer) {
        return null;
    }
}
