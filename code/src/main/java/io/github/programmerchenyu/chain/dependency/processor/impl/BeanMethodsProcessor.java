package io.github.programmerchenyu.chain.dependency.processor.impl;

import io.github.programmerchenyu.beans.BeanDefinition;
import io.github.programmerchenyu.beans.factory.annotation.Bean;
import io.github.programmerchenyu.chain.dependency.DependencyInjectionChain;
import io.github.programmerchenyu.chain.dependency.processor.DependencyInjectionProcessor;
import io.github.programmerchenyu.enums.chain.ProcessorEnum;

import java.util.Arrays;
import java.util.Map;

/**
 * @author 爱吃小鱼的橙子
 */
public class BeanMethodsProcessor implements DependencyInjectionProcessor {
    @Override
    public Boolean process(DependencyInjectionChain processChain, Integer processIndex, Object arg, Map<String, BeanDefinition> beanDefinitions) {
        Class<?> configClass = (Class<?>) arg;
        processBeanMethods(configClass, beanDefinitions);
        return processChain.process(null, ProcessorEnum.END.getIndex(), null, null);
    }

    private void processBeanMethods(Class<?> configClass, Map<String, BeanDefinition> beanDefinitions) {
        Arrays.stream(configClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Bean.class))
                .forEach(method -> {
                    String beanName = method.getAnnotation(Bean.class).value();
                    if (beanName.isEmpty()) {
                        beanName = determineBeanName(method.getReturnType());
                    }
                    BeanDefinition definition = new BeanDefinition(method);
                    beanDefinitions.putIfAbsent(beanName, definition);
                });
    }
}
