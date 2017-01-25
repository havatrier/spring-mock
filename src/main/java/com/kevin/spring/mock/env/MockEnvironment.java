package com.kevin.spring.mock.env;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * To store global spring-mock environment variables
 * <p>
 * <p>
 * Created by shuchuanjun on 17/1/8.
 */
public class MockEnvironment {
    private Map<String, Object> envVariables; // env variables

    private MockEnvironment() {
        envVariables = Maps.newHashMap();
    }

    private static class InstanceHolder {
        public static MockEnvironment instance = new MockEnvironment();
    }
    public static MockEnvironment getInstance() {
        return InstanceHolder.instance;
    }

    public void addEnvVariable(String name, String value) {
        envVariables.put(name, value);
    }

    public Map<String, Object> getEnvVariables() {
        return envVariables;
    }
}
