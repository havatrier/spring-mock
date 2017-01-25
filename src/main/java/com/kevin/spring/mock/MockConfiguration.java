package com.kevin.spring.mock;

import com.kevin.spring.mock.evaluator.JSONReturnMockEvaluator;
import com.kevin.spring.mock.evaluator.ReturnMockEvaluator;
import org.springframework.context.ApplicationContext;

/**
 * Global configurations for spring-mock (currently mainly used to store the
 * {@link ReturnMockEvaluator} of {@link com.kevin.spring.mock.cfg.ReturnMock ReturnMock})
 * <p>
 * <p>
 *
 * Created by shuchuanjun on 17/1/8.
 */
public class MockConfiguration {
    private ReturnMockEvaluator returnMockEvaluator;
    private ApplicationContext applicationContext;

    private MockConfiguration() {
        this.returnMockEvaluator = new JSONReturnMockEvaluator();
    }
    private static class ConfigurationHolder {
        public static MockConfiguration configuration = new MockConfiguration();
    }

    public static MockConfiguration getConfiguration() {
        return ConfigurationHolder.configuration;
    }

    public ReturnMockEvaluator getReturnMockEvaluator() {
        return returnMockEvaluator;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
