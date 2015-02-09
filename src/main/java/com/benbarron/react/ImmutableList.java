package com.benbarron.react;

import java.util.Iterator;

public class ImmutableList<T> implements Iterable<T> {

    private final T[] items;

    private ImmutableList(T[] items) {
        this.items = items;
    }

    public ImmutableList<T> add(T item) {
        return null;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {

            private int position = 0;

            @Override
            public boolean hasNext() {
                return position < items.length;
            }

            @Override
            public T next() {
                return items[position++];
            }
        };
    }

    public ImmutableList<T> remove(T item) {
        return null;
    }

    public int size() {
        return items.length;
    }


    public static <T> ImmutableList<T> from(T[] items) {
        return new ImmutableList<>(items);
    }
}
