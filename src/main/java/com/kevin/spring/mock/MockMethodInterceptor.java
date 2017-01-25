package com.kevin.spring.mock;

import com.kevin.spring.mock.cfg.MethodMockConfig;
import com.kevin.spring.mock.cfg.ServiceMockConfig;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Created by shuchuanjun on 17/1/6.
 */
public class MockMethodInterceptor implements MethodInterceptor {
    private ServiceMockConfig serviceMockConfig;

    public MockMethodInterceptor(ServiceMockConfig serviceMockConfig) {
        this.serviceMockConfig = serviceMockConfig;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        MethodMockConfig methodMock = serviceMockConfig.getApplicableMethodMockConfig(invocation);
        if (methodMock != null) {
            Object returnVal = tryInvokeWithMethodMock(methodMock, invocation.getArguments());
            if (returnVal == null) { // No ReturnMock is applicable, invoke original method
                return invocation.proceed();
            }
            return returnVal;
        }
        return invocation.proceed();
    }

    private Object tryInvokeWithMethodMock(MethodMockConfig methodMock, Object[] arguments) {
        return methodMock.invoke(arguments);
    }

}
