package com.benbarron.react.lang;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Provides a mechanism for releasing resources.
 */
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
    static Closeable from(Iterable<Closeable> closeables) {
        AtomicBoolean isClosed = new AtomicBoolean(false);
        return () -> {
            if (isClosed.compareAndSet(false, true)) {
                for (Closeable closeable: closeables) {
                    try {
                        closeable.close();
                    } catch (Exception e) {
                        // TODO: add to some throwable
                    }
                }
            }
        };
    }
}
