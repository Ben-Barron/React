package com.benbarron.react;

import java.util.Collection;

/**
 * Provides a mechanism for releasing resources.
 */
@FunctionalInterface
public interface Closeable {

    /**
     * The empty closeable.
     */
    static final Closeable EMPTY = () -> { };

    /**
     * Performs application-defined tasks associated with freeing, releasing, or resetting resources.
     */
    void close();


    /**
     * Returns the empty closeable.
     * @return The empty closeable (Closeable.EMPTY).
     */
    static Closeable empty() {
        return EMPTY;
    }

    /**
     * Returns a closeable which closes all underlying closeables.
     * @param closeables Collection of underlying closeables.
     * @return A closeable that closes the underlying closeables.
     */
    static Closeable from(Collection<Closeable> closeables) {
        return () -> closeables.forEach(Closeable::close);
    }
}
