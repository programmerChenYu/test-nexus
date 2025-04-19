package io.github.programmerchenyu.beans;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * @author 爱吃小鱼的橙子
 */
public class BeanDefinition {
    private final Class<?> clazz;
    private final Method factoryMethod;
    private final Constructor<?> constructor;

    public BeanDefinition(Class<?> clazz) throws NoSuchMethodException {
        this.clazz = clazz;
        this.factoryMethod = null;
        // 默认无参构造
        this.constructor = clazz.getDeclaredConstructor();
    }

    public BeanDefinition(Method factoryMethod) {
        this.clazz = factoryMethod.getReturnType();
        this.factoryMethod = factoryMethod;
        this.constructor = null;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Method getFactoryMethod() {
        return factoryMethod;
    }

    public boolean hasMethod() {
        return factoryMethod != null;
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }
}
