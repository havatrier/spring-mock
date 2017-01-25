package com.kevin.spring.mock.provider;

import com.kevin.spring.mock.cfg.ServiceMockConfig;

/**
 * Interface to read mock configuration
 * <p>
 * <p>
 * Created by shuchuanjun on 17/1/6.
 */
public interface MockDataProvider {
    /**
     * get {@link ServiceMockConfig} by bean's type
     * @param clazz the type of the bean
     * @return the <tt>ServiceMockConfig</tt> corresponding to the bean's type; returns null if none
     */
    ServiceMockConfig getServiceMockConfig(Class<?> clazz);
}
