package com.benbarron.react;

import com.benbarron.react.function.Func1;
import com.benbarron.react.internal.exception.ObservableGenerationException;
import com.benbarron.react.lang.Closeable;

class ObservableGeneratingObservable<O> implements Observable<O> {

    private final Func1<Observer<O>, Closeable> onGenerate;

    ObservableGeneratingObservable(Func1<Observer<O>, Closeable> onGenerate) {
        this.onGenerate = onGenerate;
    }

    @Override
    public Closeable subscribe(Observer<O> observer) {
        try {
            return onGenerate.run(observer);
        } catch (Exception e) {
            throw new ObservableGenerationException(e);
        }
    }
}
