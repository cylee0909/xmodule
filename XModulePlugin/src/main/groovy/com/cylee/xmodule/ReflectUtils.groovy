package com.cylee.xmodule
import java.lang.reflect.Field;

class ReflectUtils {
    static Field getFieldByName(Class<?> aClass, String name) {
        Class<?> currentClass = aClass
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField(name)
            } catch (NoSuchFieldException e) {
                // ignored.
            }
            currentClass = currentClass.getSuperclass()
        }
        return null
    }
}