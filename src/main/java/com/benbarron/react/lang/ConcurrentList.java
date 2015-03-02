package com.benbarron.react.lang;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

public class ConcurrentList<T> extends AbstractCollection<T> {

    private final AtomicReference<ImmutableList<T>> list = new AtomicReference<>(ImmutableList.empty());

    @Override
    public boolean add(T t) {
        list.getAndUpdate(l -> l.add(t));
        return true;
    }

    @Override
    public void clear() {
        list.set(ImmutableList.empty());
    }

    @Override
    public Iterator<T> iterator() {
        return list.get().iterator();
    }

    @Override
    public boolean remove(Object o) {
        ImmutableList<T> prev, next;

        do {
            prev = list.get();
            next = prev.remove(o);
        } while (!list.compareAndSet(prev, next));

        return (next.size() < prev.size());
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;

        for (Object o : c) {
            if (remove(o)) {
                modified = true;
            }
        }

        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        // TODO: implement
        throw new UnsupportedOperationException("retainAll");
    }

    @Override
    public int size() {
        return list.get().size();
    }

}
