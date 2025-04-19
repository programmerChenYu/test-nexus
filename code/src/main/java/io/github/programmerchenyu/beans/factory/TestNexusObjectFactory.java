package io.github.programmerchenyu.beans.factory;

import io.github.programmerchenyu.base.annotion.TestNexus;
import io.github.programmerchenyu.beans.BeanDefinition;
import io.github.programmerchenyu.beans.factory.annotation.Destroy;
import io.github.programmerchenyu.constants.context.ContextAttribute;
import org.testng.IObjectFactory;
import org.testng.ITestContext;

import java.beans.Introspector;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author 爱吃小鱼的橙子
 */
public class TestNexusObjectFactory implements IObjectFactory {

    private BeanFactory beanFactory;

    private ITestContext context;

    public TestNexusObjectFactory(ITestContext context) {
        this.context = context;
    }

    @Override
    public Object newInstance(Constructor constructor, Object... args) {
        Class<?> testClass = constructor.getDeclaringClass();
        if (testClass.isAnnotationPresent(TestNexus.class)) {
            beanFactory = (BeanFactory) context.getAttribute(ContextAttribute.BEAN_FACTORY);
            if (beanFactory == null) {
                beanFactory = new BeanFactory(testClass.getAnnotation(TestNexus.class).classes());
                context.setAttribute(ContextAttribute.BEAN_FACTORY, beanFactory);
            }
            registerTestClassAsBean(testClass, beanFactory);
            // 将Class转换为Bean名称（遵循与BeanFactory一致的命名规则）
            String beanName = Introspector.decapitalize(testClass.getSimpleName());
            // 使用字符串名称获取Bean
            return beanFactory.getBean(beanName);
        }
        try {
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test instance", e);
        }
    }

    private void registerTestClassAsBean(Class<?> testClass, BeanFactory beanFactory) {
        // 生成Bean名称
        String beanName = Introspector.decapitalize(testClass.getSimpleName());
        try {
            // 创建BeanDefinition（组件类类型）
            BeanDefinition definition = new BeanDefinition(testClass);
            // 注册到BeanFactory（覆盖已有定义）
            Field beanDefinitions = beanFactory.getClass().getDeclaredField("beanDefinitions");
            beanDefinitions.setAccessible(true);
            ((Map<String, BeanDefinition>)beanDefinitions.get(beanFactory)).putIfAbsent(beanName, definition);
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to access BeanFactory internal state", e);
        }
    }

    public void destroyTestNexusObjectFactory() {
        try {
            Field destroyStackField = beanFactory.getClass().getDeclaredField("destroyStack");
            destroyStackField.setAccessible(true);
            LinkedList<Object> destroyStack = (LinkedList<Object>) destroyStackField.get(beanFactory);
            while (!destroyStack.isEmpty()) {
                Object bean = destroyStack.removeLast();
                Class<?> clazz = bean.getClass();
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(Destroy.class)) {
                        // 如果用户写了销毁前的逻辑，此时执行
                        method.setAccessible(true);
                        if (method.getParameters().length > 0) {
                            throw new RuntimeException("the methods marked with the @Destroy annotation must not have parameters");
                        }
                        method.invoke(bean);
                    }
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("TestNexus encounters any issues, please contact the author");
        }
    }
}
