package com.benbarron.react;

import java.util.concurrent.atomic.AtomicBoolean;

public class Example {

    private AtomicBoolean isValueSet = new AtomicBoolean(false);
    private Object value = null;

    private void initializeValue(Object value) {
        // as you can see, value can only ever be set once.
        if (isValueSet.compareAndSet(false, true)) {
            UnsafeUtil.setValue(this, "value", value);
        }
    }

    public Object getValue() {
        return value;
    }


    public static void main(String[] args) {
        Example e = new Example();

        Object o = new Object();

        e.initializeValue(o);

        if (o.equals(e.getValue())) {
            System.out.println("yay");
        }
    }
}