package com.benbarron.react.lang;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ImmutableList<T> implements Iterable<T> {

    private final T[] items;

    private ImmutableList(T[] items) {
        this.items = items.clone();
    }

    public ImmutableList<T> add(T item) {
        T[] newItems = Arrays.copyOf(items, items.length + 1);
        newItems[items.length] = item;

        return from(newItems);
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
                if (hasNext()) {
                    return items[position++];
                }

                throw new NoSuchElementException();
            }
        };
    }

    public ImmutableList<T> remove(T item) {
        T[] newItems = items;

        for (int i = 0; i < items.length; i++) {
            if (items[i].equals(item)) {
                newItems = Arrays.copyOf(items, items.length - 1);
                System.arraycopy(items, i + 1, newItems, i, newItems.length - i);
                break;
            }
        }

        return from(newItems);
    }

    public int size() {
        return items.length;
    }


    public static <T> ImmutableList<T> empty() {
        return from();
    }

    @SafeVarargs
    public static <T> ImmutableList<T> from(T... items) {
        return new ImmutableList<>(items);
    }
}
