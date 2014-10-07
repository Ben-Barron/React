package com.benbarron.react;

import java.util.function.Consumer;

public interface Observables {

	static <T> Observable<T> generate(Consumer<Observer<T>> observer) {
		return new DefaultObservable<>((T item, Observer<T> o) -> observer.accept(o));
	}
}
