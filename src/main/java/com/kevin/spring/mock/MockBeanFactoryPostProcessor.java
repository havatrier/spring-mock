package com.kevin.spring.mock;

import com.kevin.spring.mock.provider.MockDataProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * To automatically register {@link MockBeanPostProcessor} to {@link org.springframework.beans.factory.BeanFactory BeanFactory}
 * <p>
 * <p>
 *
 * Created by shuchuanjun on 17/1/6.
 */
public class MockBeanFactoryPostProcessor implements BeanFactoryPostProcessor, ApplicationContextAware {
    private MockDataProvider mockDataProvider;
    private boolean proxyTargetClass;
    private ApplicationContext applicationContext;
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        beanFactory.registerSingleton("mtMockBeanPostProcessor", new MockBeanPostProcessor(proxyTargetClass, mockDataProvider));
        MockConfiguration.getConfiguration().setApplicationContext(this.applicationContext);
    }

    public MockDataProvider getMockDataProvider() {
        return mockDataProvider;
    }

    public void setMockDataProvider(MockDataProvider mockDataProvider) {
        this.mockDataProvider = mockDataProvider;
    }

    public boolean isProxyTargetClass() {
        return proxyTargetClass;
    }

    public void setProxyTargetClass(boolean proxyTargetClass) {
        this.proxyTargetClass = proxyTargetClass;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
