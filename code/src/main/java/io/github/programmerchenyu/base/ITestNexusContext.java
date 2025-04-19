package io.github.programmerchenyu.base;

import io.github.programmerchenyu.beans.factory.TestNexusObjectFactory;
import io.github.programmerchenyu.constants.context.ContextAttribute;
import org.testng.IObjectFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.ObjectFactory;

/**
 * @author 爱吃小鱼的橙子
 */
public interface ITestNexusContext {

    @ObjectFactory
    default IObjectFactory setObjectFactory(ITestContext context) {
        TestNexusObjectFactory objectFactory = (TestNexusObjectFactory) context.getAttribute(ContextAttribute.OBJECT_FACTORY);
        if (objectFactory != null) {
            return objectFactory;
        }
        TestNexusObjectFactory testNexusObjectFactory = new TestNexusObjectFactory(context);
        context.setAttribute(ContextAttribute.OBJECT_FACTORY, testNexusObjectFactory);
        return testNexusObjectFactory;
    }

    @AfterSuite
    default void destroyObjectFactory(ITestContext context) {
        TestNexusObjectFactory objectFactory = (TestNexusObjectFactory) context.getAttribute(ContextAttribute.OBJECT_FACTORY);
        objectFactory.destroyTestNexusObjectFactory();
    }

}
