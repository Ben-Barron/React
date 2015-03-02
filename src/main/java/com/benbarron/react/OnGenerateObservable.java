package com.benbarron.react;

import com.benbarron.react.function.Func1;
import com.benbarron.react.internal.exception.OnGenerateException;
import com.benbarron.react.lang.Closeable;

class OnGenerateObservable<O> implements Observable<O> {

    private final Func1<Observer<O>, Closeable> onGenerate;

    OnGenerateObservable(Func1<Observer<O>, Closeable> onGenerate) {
        this.onGenerate = onGenerate;
    }

    @Override
    public Closeable subscribe(Observer<O> observer) {
        try {
            return onGenerate.run(observer);
        } catch (Exception e) {
            throw new OnGenerateException(e);
        }
    }
}
