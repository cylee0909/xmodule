package com.cylee.xmodule;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ApiFinder {
    public static final String DEF_IMPL_SUFFIX = "_Impl";

    public static <T> T findApi(Class<T> interfaceClass) {
        return findApiWithSuffix(interfaceClass, DEF_IMPL_SUFFIX);
    }

    public static <T> T findApiWithSuffix(Class<T> interfaceClass, String suffix) {
        String name = interfaceClass.getName();
        return findApiAbsolute(interfaceClass, name + suffix);
    }

    public static <T> T findApiAbsolute(Class<T> interfaceClass, String absolutePath) {
        try {
            Class clazz = Class.forName(absolutePath);
            if (!INoProguard.class.isAssignableFrom(interfaceClass)) {
                throw new RuntimeException("interface "+interfaceClass.getName()+" must extends com.baidu.homework.common.utils.INoProguard");
            }
            if (interfaceClass.isAssignableFrom(clazz)) {
                Constructor<T> constructor = clazz.getConstructor(new Class[0]);
                if (constructor != null) {
                    T result = constructor.newInstance(new Object[0]);
                    if (result != null) {
                        return result;
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
