package com.benbarron.react.lang;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

public class ConcurrentList<T> extends AbstractCollection<T> {

    private final AtomicReference<ImmutableList<T>> list = new AtomicReference<>(ImmutableList.empty());

    @Override
    public Iterator<T> iterator() {
        return list.get().iterator();
    }

    @Override
    public int size() {
        return list.get().size();
    }

    @Override
    public boolean add(T t) {
        list.getAndUpdate(l -> l.add(t));
        return true;
    }

    @Override
    public boolean remove(Object o) {
        list.getAndUpdate(l -> l.remove((T)o));
        return super.remove(o);
    }
}
