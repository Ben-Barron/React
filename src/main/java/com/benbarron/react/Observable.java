package com.benbarron.react;

import com.benbarron.react.lang.Closeable;
import com.benbarron.react.lang.ImmutableList;
import com.benbarron.react.operator.Any;
import com.benbarron.react.operator.Distinct;
import com.benbarron.react.operator.DistinctUntilChanged;

import java.util.Arrays;
import java.util.function.*;

/**
 * Represents a stream of push-based notifications.
 * @param <T> The object that provides notification information.
 */
public interface Observable<T> {

    default Observable<Boolean> any(Predicate<T> predicate) {
        return observeAction(new Any<>(predicate));
    }

    default Observable<T> distinct() {
        return observeAction(new Distinct<>());
    }

    default Observable<T> distinctUntilChanged() {
        return observeAction(new DistinctUntilChanged<>());
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

    default <R> Observable<R> observeAction(Function<Observer<R>, Observer<T>> action) {
        return new DefaultObservable<>(ImmutableList.from(this), null, action);
    }

    default ConnectableObservable<T> publish() {
        return new DefaultConnectableObservable<>(Arrays.asList(this));
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

    default Observable<T> subscribeAction(BiFunction<Iterable<Observable<T>>, Observer<T>, Closeable> action) {
        return new DefaultObservable<>(ImmutableList.from(this), action, null);
    }


    static <T> Observable<T> generate(Consumer<Observer<T>> observer) {
        return generate(o -> { observer.accept(o); return Closeable.empty(); });
    }

    static <T> Observable<T> generate(Function<Observer<T>, Closeable> observer) {
        return new DefaultObservable<>(null, (co, o) -> observer.apply(o), null);
    }
}
