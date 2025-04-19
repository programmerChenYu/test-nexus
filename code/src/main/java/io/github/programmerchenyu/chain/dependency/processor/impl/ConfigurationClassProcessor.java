package io.github.programmerchenyu.chain.dependency.processor.impl;

import io.github.programmerchenyu.base.ClassScanner;
import io.github.programmerchenyu.beans.BeanDefinition;
import io.github.programmerchenyu.beans.factory.annotation.Component;
import io.github.programmerchenyu.beans.factory.annotation.ComponentScan;
import io.github.programmerchenyu.beans.factory.annotation.Configuration;
import io.github.programmerchenyu.chain.dependency.DependencyInjectionChain;
import io.github.programmerchenyu.chain.dependency.processor.DependencyInjectionProcessor;
import io.github.programmerchenyu.enums.chain.ProcessorEnum;

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 配置类处理器
 * @author 爱吃小鱼的橙子
 */
public class ConfigurationClassProcessor implements DependencyInjectionProcessor {
    @Override
    public Boolean process(DependencyInjectionChain processChain, Integer processIndex, Object arg, Map<String, BeanDefinition> beanDefinitions) {
        Class<?>[] configClasses = (Class<?>[]) arg;
        List<Boolean> res = new ArrayList<>();
        for (Class<?> configClass : configClasses) {
            processConfigurationClass(configClass, beanDefinitions);
            res.add(processChain.process(processChain, ProcessorEnum.BEAN_METHODS.getIndex(), configClass, beanDefinitions));
        }
        for (Boolean value : res) {
            if (!value) {
                return false;
            }
        }
        return true;
    }

    private void processConfigurationClass(Class<?> configClass, Map<String, BeanDefinition> beanDefinitions) {
        if (configClass.isAnnotationPresent(Configuration.class)) {
            try {
                beanDefinitions.putIfAbsent(Introspector.decapitalize(configClass.getSimpleName()), new BeanDefinition(configClass));
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            ComponentScan componentScan = configClass.getAnnotation(ComponentScan.class);
            if (componentScan != null && componentScan.basePackages().length > 0) {
                Arrays.stream(componentScan.basePackages())
                        .forEach(basePackage -> scanPackageForComponents(basePackage, beanDefinitions));
            }
        }
    }

    private void scanPackageForComponents(String basePackage, Map<String, BeanDefinition> beanDefinitions) {
        List<Class<?>> classes = ClassScanner.scanClasses(basePackage);
        classes.forEach(clazz -> registerComponentClass(clazz, beanDefinitions));
    }

    private void registerComponentClass(Class<?> clazz, Map<String, BeanDefinition> beanDefinitions) {
        if (clazz.isAnnotationPresent(Component.class)) {
            String beanName = determineBeanName(clazz);
            try {
                BeanDefinition definition = new BeanDefinition(clazz);
                beanDefinitions.putIfAbsent(beanName, definition);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

}
