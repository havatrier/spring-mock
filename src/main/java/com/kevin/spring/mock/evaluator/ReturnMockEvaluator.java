package com.kevin.spring.mock.evaluator;

import com.kevin.spring.mock.cfg.MethodMockConfig;
import com.kevin.spring.mock.cfg.ReturnMock;

/**
 * Created by shuchuanjun on 17/1/7.
 */
public interface ReturnMockEvaluator {
    /**
     * Evaluate text content to an Object as mock result
     * @param methodMock  MethodMockConfig
     * @param returnMock ReturnMock
     * @param arguments  method invocation arguments
     * @return java Object
     */
    Object evaluate(MethodMockConfig methodMock, ReturnMock returnMock, Object[] arguments);
}
