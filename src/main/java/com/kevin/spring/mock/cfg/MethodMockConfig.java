package com.kevin.spring.mock.cfg;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.kevin.spring.mock.MockConfiguration;
import com.kevin.spring.mock.evaluator.ReturnMockEvaluator;
import com.kevin.spring.mock.evaluator.TestExprEvaluator;
import com.kevin.spring.mock.exception.MockRuntimeException;

import java.util.List;

/**
 * Created by shuchuanjun on 17/1/6.
 */
public class MethodMockConfig {
    private String methodName;
    private List<ParamMeta> params;
    private JavaType returnType;
    private List<ReturnMock> returnMocks;

    private ReturnMockEvaluator returnMockEvaluator;
    public MethodMockConfig() {
        params = Lists.newArrayList();
        returnMocks = Lists.newArrayList();
        returnMockEvaluator = MockConfiguration.getConfiguration().getReturnMockEvaluator();
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<ParamMeta> getParams() {
        return params;
    }

    public void setParams(List<ParamMeta> params) {
        this.params = params;
    }

    public JavaType getReturnType() {
        return returnType;
    }

    public void setReturnType(JavaType returnType) {
        this.returnType = returnType;
    }

    public List<ReturnMock> getReturnMocks() {
        return returnMocks;
    }

    public void setReturnMocks(List<ReturnMock> returnMocks) {
        this.returnMocks = returnMocks;
    }

    public void addParam(ParamMeta param) {
        this.params.add(param);
    }

    public void addReturnMock(ReturnMock returnMock) {
        this.returnMocks.add(returnMock);
    }


    /**
     * mock invoke the proxy method to return mock data
     * @param arguments
     * @return if no ReturnMock is applicable for the params return null;
     */
    public Object invoke(Object[] arguments) {
        if (arguments.length == this.params.size()) {
            ReturnMock returnMock = findReturnMock(arguments);
            if (returnMock != null) {
                Object retObj = returnMockEvaluator.evaluate(this, returnMock, arguments);
                Preconditions.checkState(retObj != null, "Evaluating " + returnMock + " gets null result");
                return retObj;
            }
            return null;
        }
        throw new MockRuntimeException("Unexpected params number used to invoke():" +
                " it needs " + this.params.size() + ", while provided with " + arguments.length);
    }

    private ReturnMock findReturnMock(Object[] arguments) {
        TestExprEvaluator evaluator = TestExprEvaluator.getEvaluator(params);
        List<ReturnMock> emptyTestExprReturnMocks = Lists.newArrayList();
        for (ReturnMock returnMock : returnMocks) {
            String testExpr = returnMock.getTestExpr();
            if (Strings.isNullOrEmpty(testExpr)) { // test expr is empty, applicable
                emptyTestExprReturnMocks.add(returnMock);
            } else if (evaluator.evaluateTestExpr(arguments, returnMock.getTestExpr())) { // pass test expr, use it directly
                return returnMock;
            }
        }
        if (!emptyTestExprReturnMocks.isEmpty()) { // if there is ReturnMock whose testExpr is empty, use the first one
            return emptyTestExprReturnMocks.get(0);
        }
        return null;
    }

    @Override
    public String toString() {
        return "MethodMockConfig{" +
                "methodName='" + methodName + '\'' +
                ", params=" + params +
                ", returnType=" + returnType +
                ", returnMocks=" + returnMocks +
                '}';
    }
}
