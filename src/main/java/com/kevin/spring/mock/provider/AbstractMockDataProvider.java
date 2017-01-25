package com.kevin.spring.mock.provider;

import com.google.common.base.Preconditions;
import com.kevin.spring.mock.cfg.ServiceMockConfig;

import java.util.List;
import java.util.Map;

/**
 * abstract implementation of {@link MockDataProvider}
 * <p>
 * <p>
 * Created by shuchuanjun on 17/1/6.
 */
public abstract class AbstractMockDataProvider implements MockDataProvider {
    protected Map<Class<?>, ServiceMockConfig>  typeConfigMap;
    protected List<ServiceMockConfig> configs;

    /**
     * To initialize {@link #typeConfigMap} and {@link #configs}
     */
    protected abstract void init();

    @Override
    public ServiceMockConfig getServiceMockConfig(Class<?> clazz) {
        Preconditions.checkState(typeConfigMap != null && configs != null,
                "Not initialized yet!");

        if (typeConfigMap.containsKey(clazz)) {
            return typeConfigMap.get(clazz);
        }
        for (ServiceMockConfig config : configs) {
            Class<?> type = config.getServiceType();
            if (type.isAssignableFrom(clazz)) {
                return config;
            }
        }
        return null;
    }

    protected void registerServiceConfig(ServiceMockConfig config) {
        Preconditions.checkState(typeConfigMap != null && configs != null,
                "Not initialized yet!");
        typeConfigMap.put(config.getServiceType(), config);
        configs.add(config);
    }
}
