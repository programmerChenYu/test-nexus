package io.github.programmerchenyu.beans.factory;

import io.github.programmerchenyu.beans.BeanDefinition;
import io.github.programmerchenyu.beans.exception.BeanCreationException;
import io.github.programmerchenyu.beans.factory.annotation.Autowired;
import io.github.programmerchenyu.beans.factory.annotation.InitAfter;
import io.github.programmerchenyu.beans.factory.annotation.InitBefore;
import io.github.programmerchenyu.chain.dependency.DependencyInjectionChain;
import io.github.programmerchenyu.enums.chain.ProcessorEnum;

import java.beans.Introspector;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author 爱吃小鱼的橙子
 */
public class BeanFactory {

    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>();
    private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>();
    private final Map<String, BeanDefinition> beanDefinitions = new ConcurrentHashMap<>();
    private final LinkedList<Object> destroyStack = new LinkedList<>();

    public BeanFactory(Class<?>[] configClasses) {
        DependencyInjectionChain dependencyInjectionChain = new DependencyInjectionChain();
        dependencyInjectionChain.process(dependencyInjectionChain, ProcessorEnum.CONFIGURATION_CLASS.getIndex(), configClasses, beanDefinitions);
    }

    public Object getBean(String beanName) {
        return getBean(beanName, true);
    }

    /**
     * 未解决循环依赖问题
     * @param beanName
     * @param isSingle
     * @return
     */
    Object getBean(String beanName, boolean isSingle) {
        if (singletonObjects.containsKey(beanName) && isSingle) {
            return singletonObjects.get(beanName);
        } else if (earlySingletonObjects.containsKey(beanName) && isSingle) {
            return earlySingletonObjects.get(beanName);
        }

        BeanDefinition definition = beanDefinitions.get(beanName);
        if (definition == null) {
            throw new BeanCreationException("No such bean: " + beanName);
        }

        Object bean = createBeanInstance(definition);
        // 初始化对象时将该对象放入销毁栈中，当对象开始销毁时再依次销毁
        destroyStack.addLast(bean);
        // 准备执行对象初始化前用户自定义的逻辑
        initBeforeMethodExecute(bean);
        earlySingletonObjects.put(beanName, bean);
        applyDependencies(bean, definition);

        if (isSingle) {
            singletonObjects.put(beanName, bean);
        }
        earlySingletonObjects.remove(beanName);
        initAfterMethodExecute(bean);
        return bean;
    }

    private Object createBeanInstance(BeanDefinition definition) {
        try {
            if (definition.hasMethod()) {
                Method factoryMethod = definition.getFactoryMethod();
                Object targetObject = getFactoryMethodTarget(definition);
                return factoryMethod.invoke(targetObject);
            } else {
                Constructor<?> constructor = definition.getConstructor();
                constructor.setAccessible(true);
                return constructor.newInstance();
            }
        } catch (Exception e) {
            throw new BeanCreationException("Error creating bean instance", e);
        }
    }

    private Object getFactoryMethodTarget(BeanDefinition definition) {
        if (Modifier.isStatic(definition.getFactoryMethod().getModifiers())) {
            // 静态方法无需目标对象
            return null;
        }
        // 非静态工厂方法需获取配置类实例（假设配置类已注册为单例）
        String configBeanName = Introspector.decapitalize(definition.getFactoryMethod().getDeclaringClass().getSimpleName());
        // 从容器获取配置类实例
        return getBean(configBeanName);
    }

    private void applyDependencies(Object bean, BeanDefinition definition) {
        Class<?> clazz = bean.getClass();
        // 构造函数注入
        processConstructorInjection(clazz, bean);
        // 字段注入
        processFieldInjection(clazz, bean);
        // 方法注入
        processMethodInjection(clazz, bean);
    }

    private void processConstructorInjection(Class<?> clazz, Object bean) {
        Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();
        Arrays.stream(clazz.getDeclaredConstructors())
                .filter(c -> c.isAnnotationPresent(Autowired.class))
                .findFirst()
                .ifPresent(constructor -> {
                    Autowired autowired = constructor.getAnnotation(Autowired.class);
                    Object[] args = getDependencyArgs(constructor.getParameterTypes(), autowired);
                    invokeConstructor(bean, constructor, args);
                });
    }

    private void processFieldInjection(Class<?> clazz, Object bean) {
        List<Field> fields = Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Autowired.class)).collect(Collectors.toList());
        for (Field field : fields) {
            Autowired autowired = field.getAnnotation(Autowired.class);
            Object dependency = getDependency(field.getType(), autowired);
            injectField(bean, field, dependency);
        }
    }

    private void processMethodInjection(Class<?> clazz, Object bean) {
        Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Autowired.class) && m.getParameterCount() > 0)
                .forEach(method -> {
                    Autowired autowired = method.getAnnotation(Autowired.class);
                    Object[] args = getDependencyArgs(method.getParameterTypes(), autowired);
                    invokeMethod(bean, method, args);
                });
    }

    private Object[] getDependencyArgs(Class<?>[] parameterTypes, Autowired autowired) {
        return Arrays.stream(parameterTypes)
                .map(type -> getDependency(type, autowired))
                .toArray();
    }

    private Object getDependency(Class<?> type, Autowired autowired) {
        String beanName = getBeanName(autowired, type);
        if (type.isInterface() && "".equals(autowired.name())) {
            beanName = transformInterface2ImplKey(type);
        }
        return getBean(beanName, autowired.singleton());
    }

    private String getBeanName(Autowired autowired, Class<?> type) {
        return !autowired.name().isEmpty() ? autowired.name() : determineBeanName(type);
    }

    private void invokeConstructor(Object bean, Constructor<?> constructor, Object[] args) {
        try {
            // 判断是否要执行构造还是说对其的属性进行注入
            if (args.length > 0) {
                for (Field field : bean.getClass().getDeclaredFields()) {
                    for (Object arg : args) {
                        if (field.getType().equals(arg.getClass())) {
                            injectField(bean, field, arg);
                        }
                    }
                }
            } else {
                constructor.setAccessible(true);
                constructor.newInstance(args);
            }
        } catch (Exception e) {
            throw new BeanCreationException("Constructor injection failed", e);
        }
    }

    private void injectField(Object bean, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(bean, value);
        } catch (Exception e) {
            throw new BeanCreationException("Field injection failed", e);
        }
    }

    private void invokeMethod(Object bean, Method method, Object[] args) {
        try {
            method.setAccessible(true);
            method.invoke(bean, args);
        } catch (Exception e) {
            throw new BeanCreationException("Method injection failed", e);
        }
    }

    private String determineBeanName(Class<?> type) {
        // 默认策略：实现类名首字母小写
        return Introspector.decapitalize(type.getSimpleName());
    }

    private String transformInterface2ImplKey(Class<?> type) {
        List<BeanDefinition> implBeanDefinitions = new ArrayList<>();
        for (BeanDefinition beanDefinition : beanDefinitions.values()) {
            if (type.isAssignableFrom(beanDefinition.getClazz())) {
                implBeanDefinitions.add(beanDefinition);
            }
        }
        if (implBeanDefinitions.size() == 1) {
            return Introspector.decapitalize(implBeanDefinitions.get(0).getClazz().getSimpleName());
        } else if (implBeanDefinitions.size() > 1) {
            throw new RuntimeException("TestNexus 检测到该接口有多个实现类，请使用 @Autowired 注解中的 name 属性来指定使用具体的实现类");
        } else {
            throw new RuntimeException(type + " no such bean");
        }
    }

    private void initBeforeMethodExecute(Object bean) {
        Method[] methods = bean.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(InitBefore.class)) {
                // 如果方法上标注有该初始化注解
                method.setAccessible(true);
                Parameter[] parameters = method.getParameters();
                if (parameters.length > 0) {
                    throw new RuntimeException("the methods marked with the @InitBefore annotation must not have parameters");
                }
                try {
                    method.invoke(bean);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException("TestNexus encounters any issues, please contact the author");
                }
            }
        }
    }

    private void initAfterMethodExecute(Object bean) {
        Method[] methods = bean.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(InitAfter.class)) {
                // 如果方法上标注有该初始化注解
                method.setAccessible(true);
                Parameter[] parameters = method.getParameters();
                if (parameters.length > 0) {
                    throw new RuntimeException("the methods marked with the @InitAfter annotation must not have parameters");
                }
                try {
                    method.invoke(bean);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException("TestNexus encounters any issues, please contact the author");
                }
            }
        }
    }

}
