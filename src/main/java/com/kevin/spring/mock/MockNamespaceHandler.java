package com.kevin.spring.mock;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Register spring-mock defined label parser
 * <p>
 * <p>
 *
 * Created by shuchuanjun on 17/1/6.
 */
public class MockNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("mock", new MockBeanDefinitionParser());
    }
}
