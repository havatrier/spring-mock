package com.kevin.spring.mock.evaluator;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.kevin.spring.mock.cfg.ParamMeta;
import com.kevin.spring.mock.env.MockEnvironment;
import com.kevin.spring.mock.exception.MockRuntimeException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.List;
import java.util.Map;

/**
 * Parse <tt>testExpr</tt> expression based on <tt>Spring SpEL</tt>
 * <p>
 * Created by shuchuanjun on 17/1/8.
 */
public class TestExprEvaluator {
    private static Map<List<ParamMeta>, TestExprEvaluator> evaluatorCache; // 使用class static Map确保全局相同的参数列表使用同一个TestExprEvaluator(效率)
    private static ExpressionParser parser;
    static {
        evaluatorCache = Maps.newConcurrentMap();
        parser = new SpelExpressionParser();
    }

    private List<ParamMeta> params;
    private Map<String, Expression> expressionCache;
    private EvaluationContext evaluationContext;


    private TestExprEvaluator(List<ParamMeta> params) {
        this.params = params;
        this.expressionCache = Maps.newConcurrentMap();
        this.evaluationContext = createEvaluationContext();
    }

    public static TestExprEvaluator getEvaluator(List<ParamMeta> params) {
        TestExprEvaluator evaluator = evaluatorCache.get(params);
        if (evaluator == null) { // 确保多线程下的效率, 虽然evaluatorCache是ConcurrentMap
            synchronized (TestExprEvaluator.class) {
                if (evaluator == null) {
                    evaluator = new TestExprEvaluator(params);
                    evaluatorCache.put(params, evaluator);
                }
            }
        }
        return evaluator;
    }

    public boolean evaluateTestExpr(Object[] arguments, String testExpr) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(testExpr), "Error: testExpr is empty");
        Preconditions.checkArgument(arguments != null);
        Preconditions.checkState(arguments.length == params.size(), "Arguments number (" +
                arguments.length + ") does not match internal params number (" + params.size() + ")");

        fillArguments2EvaluationContext(arguments);
        Expression expression = getExpressionForTestExpr(testExpr);
        Object exprValue = expression.getValue(evaluationContext);
        if (exprValue instanceof Boolean) {
            return (Boolean) exprValue;
        } else {
            throw new MockRuntimeException("Invalid evaluation result (Not boolean type) for test expr '" + testExpr + "'");
        }
    }

    private Expression getExpressionForTestExpr(String testExpr) {
        String cleanTestExpr = testExpr.trim();
        Expression expression = expressionCache.get(cleanTestExpr);
        if (expression == null) {
            expression = parser.parseExpression(testExpr);
            expressionCache.put(testExpr, expression);
        }
        return expression;
    }

    private void fillArguments2EvaluationContext(Object[] arguments) {
        for (int i = 0; i < params.size(); i++) {
            ParamMeta param = params.get(i);
            Object argument = arguments[i];
            this.evaluationContext.setVariable(param.getName(), argument);
        }
    }

    private EvaluationContext createEvaluationContext() {
        EvaluationContext evaluationContext = new StandardEvaluationContext();
        // add env variables
        Map<String, Object> envVariables = MockEnvironment.getInstance().getEnvVariables();
        for (String name : envVariables.keySet()) {
            Object value = envVariables.get(name);
            evaluationContext.setVariable(name, value);
        }
        return evaluationContext;
    }

}
