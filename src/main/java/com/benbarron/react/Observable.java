package com.benbarron.react;

import com.benbarron.react.function.*;
import com.benbarron.react.function.Action1;
import com.benbarron.react.function.Action2;
import com.benbarron.react.lang.Closeable;
import com.benbarron.react.lang.ImmutableList;
import com.benbarron.react.lang.Try;
import com.benbarron.react.operator.Any;
import com.benbarron.react.operator.Distinct;
import com.benbarron.react.operator.DistinctUntilChanged;

/**
 * Represents a stream of push-based notifications.
 * @param <T> The object that provides notification information.
 */
public interface Observable<T> {

    default Observable<T> asObservable() {
        Observable<T> self = this;
        return self::subscribe;
    }

    default Observable<Boolean> any(Predicate<T> predicate) {
        return x(new Any<>(predicate));
    }

    default Observable<T> distinct() {
        return x(new Distinct<>());
    }

    default Observable<T> distinctUntilChanged() {
        return x(new DistinctUntilChanged<>());
    }

    default Observable<T> doSubscribe(Func2<Iterable<Observable<T>>, Observer<T>, Closeable> action) {
        return new DefaultObservable<>(ImmutableList.from(this), action, null);
    }

    /*default ConnectableObservable<T> publish() {
        return new DefaultConnectableObservable<>(Arrays.asList(this));
    }*/

    default Closeable subscribe() {
        return subscribe(i -> {});
    }

    default Closeable subscribe(Action1<T> onNext) {
        return subscribe(onNext, () -> {});
    }

    default Closeable subscribe(Action1<T> onNext, Action onComplete) {
        return subscribe(onNext, onComplete, t -> { throw new RuntimeException(t); });
    }

    default Closeable subscribe(Action1<T> onNext, Action onComplete, Action1<Throwable> onError) {
        return subscribe(new Observer<T>() {
            @Override
            public void onComplete() {
                Try.ignore(onComplete::run);
            }

            @Override
            public void onError(Throwable throwable) {
                Try.ignore(() -> onError.run(throwable));
            }

            @Override
            public void onNext(T item) {
                Try.ignore(() -> onNext.run(item));
            }
        });
    }

    Closeable subscribe(Observer<T> observer);

    default <R> Observable<R> x(Action2<T, Observer<R>> action) {
        return x(o -> {
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
                    Try.run(() -> action.run(item, o));
                }
            };
        });
    }

    default <R> Observable<R> x(Func1<Observer<R>, Observer<T>> action) {
        return new DefaultObservable<>(ImmutableList.from(this), null, action);
    }


    static <T> Observable<T> generate(Action1<Observer<T>> observer) {
        return generate(o -> { observer.run(o); return Closeable.empty(); });
    }

    static <T> Observable<T> generate(Func1<Observer<T>, Closeable> observer) {
        return new DefaultObservable<>(null, (co, o) -> observer.run(o), null);
    }
}
