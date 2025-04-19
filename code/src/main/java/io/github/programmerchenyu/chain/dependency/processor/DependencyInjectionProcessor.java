package io.github.programmerchenyu.chain.dependency.processor;

import io.github.programmerchenyu.beans.BeanDefinition;
import io.github.programmerchenyu.chain.dependency.DependencyInjectionChain;

import java.beans.Introspector;
import java.util.Map;

/**
 * @author 爱吃小鱼的橙子
 */
public interface DependencyInjectionProcessor {

    Boolean process(DependencyInjectionChain processChain, Integer processIndex, Object arg, Map<String, BeanDefinition> beanDefinitions);

    default String determineBeanName(Class<?> type) {
        // 默认策略：实现类名首字母小写
        return Introspector.decapitalize(type.getSimpleName());
    }

}
