package com.benbarron.react;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

class UnsafeUtil {

    private static final Unsafe unsafe;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (sun.misc.Unsafe) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setValue(Object instance, String fieldName, Object value) {
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            unsafe.putObjectVolatile(instance, unsafe.objectFieldOffset(field), value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
