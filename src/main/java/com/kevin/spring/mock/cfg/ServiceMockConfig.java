package com.kevin.spring.mock.cfg;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.collections.map.LRUMap;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by shuchuanjun on 17/1/6.
 */
public class ServiceMockConfig {
    private Class<?> serviceType;
    private String serviceName;
    private List<MethodMockConfig> methodMocks;
    private Map<Method, MethodMockConfig> runtimeMethodMockCache;

    public ServiceMockConfig() {
        this.methodMocks = Lists.newArrayList();
        this.runtimeMethodMockCache = Collections.synchronizedMap(new LRUMap()); // use default size 100
    }

    public Class<?> getServiceType() {
        return serviceType;
    }

    public void setServiceType(Class<?> serviceType) {
        this.serviceType = serviceType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<MethodMockConfig> getMethodMocks() {
        return methodMocks;
    }

    public void setMethodMocks(List<MethodMockConfig> methodMocks) {
        this.methodMocks = methodMocks;
    }

    public void addMethodMock(MethodMockConfig methodMock) {
        this.methodMocks.add(methodMock);
    }

    public MethodMockConfig getApplicableMethodMockConfig(MethodInvocation invocation) {
        Preconditions.checkArgument(invocation != null);
        Method method = invocation.getMethod();
        // try to find in cache
        if (runtimeMethodMockCache.containsKey(method))
            return runtimeMethodMockCache.get(method);

        String methodName = method.getName();
        MethodMockConfig methodMock = findMethodMockConfigByMethodName(methodName);
        if (methodMock == null) {
            return null;
        }
        if (isParamTypeMatch(method.getParameterTypes(), methodMock)) {
            runtimeMethodMockCache.put(method, methodMock);
            return methodMock;
        }

        return null;
    }

    /**
     * determine whether have to proxy target class
     * @return
     */
    public boolean shouldProxyTargetClass() {
        return !serviceType.isInterface();
    }

    private MethodMockConfig findMethodMockConfigByMethodName(String methodName) {
        for (MethodMockConfig methodMock : methodMocks) {
            if (methodMock.getMethodName().equals(methodName)) {
                return methodMock;
            }
        }
        return null;
    }

    private boolean isParamTypeMatch(Class<?>[] paramTypes, MethodMockConfig methodMock) {
        if (methodMock.getParams().size() == paramTypes.length) {
            for (int i = 0; i < paramTypes.length; i++) {
                Class<?> paramType = paramTypes[i];
                ParamMeta paramMeta = methodMock.getParams().get(i);
                if (!paramMeta.matches(paramType))
                    return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "ServiceMockConfig{" +
                "serviceType=" + serviceType +
                ", serviceName='" + serviceName + '\'' +
                ", methodMockCfgList=" + methodMocks +
                '}';
    }
}
