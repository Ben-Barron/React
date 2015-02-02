package com.benbarron.react;

import java.util.Collection;

@FunctionalInterface
public interface Closeable {

    void close();


    static Closeable empty() {
        return () -> { };
    }

    static Closeable from(Collection<Closeable> closeables) {
        return () -> {
            for (Closeable closeable : closeables) {
                closeable.close();
            }
        };
    }
}
