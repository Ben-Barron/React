package com.benbarron.react;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface Obervable2<T> {

    //Obervable2<T> merge(Observable<T> observable);

    <R> Obervable2<R> observeAction(Function<Observer<R>, Observer<T>> action);

    default <R> Obervable2<R> action(BiConsumer<T, Observer<R>> action) {
        return observeAction(o -> { return new Observer<T>() {
            @Override
            public void onComplete() {

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onNext(T item) {
                action.accept(item, o);
            }
        }; });
    }

    AutoCloseable subscribe(Observer<T> observer);

    Obervable2<T> subscribeAction(BiFunction<Collection<Obervable2<T>>, Observer<T>, Collection<AutoCloseable>> action);

    default Obervable2<T> subscribeOn(ExecutorService executorService, boolean shutdownWithObservable) {
        return subscribeAction((co, o) -> {
            co.stream().forEach(c -> c.subscribe(o));


            return new ArrayList<>();

        });
    }
}
