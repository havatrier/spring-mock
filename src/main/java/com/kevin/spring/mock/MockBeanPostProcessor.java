package com.kevin.spring.mock;

import com.kevin.spring.mock.cfg.ServiceMockConfig;
import com.kevin.spring.mock.provider.MockDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * <tt>BeanPostProcessor</tt> for spring-mock: create AOP Proxy for a bean
 * based on its mock configuration
 * <p>
 * <p>
 *
 * Created by shuchuanjun on 17/1/6.
 */
public class MockBeanPostProcessor implements BeanPostProcessor {
    private static final Logger logger = LoggerFactory.getLogger(MockBeanPostProcessor.class);

    private MockDataProvider mockDataProvider;
    private boolean proxyTargetClass;

    public MockBeanPostProcessor() {
    }

    public MockBeanPostProcessor(boolean proxyTargetClass, MockDataProvider mockDataProvider) {
        this.proxyTargetClass = proxyTargetClass;
        this.mockDataProvider = mockDataProvider;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        ServiceMockConfig serviceMock = getServiceMockConfigForBean(bean);
        if (serviceMock != null) {
            ProxyFactory proxyFactory = new ProxyFactory(bean);
            proxyFactory.addAdvice(new MockMethodInterceptor(serviceMock));
            proxyFactory.setProxyTargetClass(shouldProxyTargetClass(serviceMock));
            Object newBean = proxyFactory.getProxy();
            logger.info("[ServiceType '{}'] use mock proxy to replace bean(name={}): {}", serviceMock.getServiceType(), beanName, bean);
            return newBean;
        }
        return bean;
    }

    private boolean shouldProxyTargetClass(ServiceMockConfig serviceMock) {
        return proxyTargetClass || serviceMock.shouldProxyTargetClass();
    }

    private ServiceMockConfig getServiceMockConfigForBean(Object bean) {
        return mockDataProvider.getServiceMockConfig(bean.getClass());
    }

    public boolean isProxyTargetClass() {
        return proxyTargetClass;
    }

    public void setProxyTargetClass(boolean proxyTargetClass) {
        this.proxyTargetClass = proxyTargetClass;
    }

    public MockDataProvider getMockDataProvider() {
        return mockDataProvider;
    }

    public void setMockDataProvider(MockDataProvider mockDataProvider) {
        this.mockDataProvider = mockDataProvider;
    }
}
