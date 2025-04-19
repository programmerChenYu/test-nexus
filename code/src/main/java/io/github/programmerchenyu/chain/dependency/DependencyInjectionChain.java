package io.github.programmerchenyu.chain.dependency;

import io.github.programmerchenyu.beans.BeanDefinition;
import io.github.programmerchenyu.chain.dependency.processor.DependencyInjectionProcessor;
import io.github.programmerchenyu.chain.dependency.processor.impl.BeanMethodsProcessor;
import io.github.programmerchenyu.chain.dependency.processor.impl.ConfigurationClassProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 爱吃小鱼的橙子
 */
public class DependencyInjectionChain {

    private List<DependencyInjectionProcessor> processorList = new ArrayList<>();

    public DependencyInjectionChain() {
        this.addProcessor(new ConfigurationClassProcessor());
        this.addProcessor(new BeanMethodsProcessor());
    }

    public DependencyInjectionChain addProcessor(DependencyInjectionProcessor processor) {
        processorList.add(processor);
        return this;
    }

    public Boolean process(DependencyInjectionChain processChain, Integer processIndex, Object arg, Map<String, BeanDefinition> beanDefinitions) {
        if (processIndex == processorList.size()) {
            return true;
        }
        DependencyInjectionProcessor dependencyInjectionProcessor = processorList.get(processIndex);
        return dependencyInjectionProcessor.process(processChain, processIndex, arg, beanDefinitions);
    }
}
