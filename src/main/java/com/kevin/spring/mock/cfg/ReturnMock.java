package com.kevin.spring.mock.cfg;

/**
 * Created by shuchuanjun on 17/1/6.
 */
public class ReturnMock {
    private String id;
    private String testExpr;
    private String returnJson;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTestExpr() {
        return testExpr;
    }

    public void setTestExpr(String testExpr) {
        this.testExpr = testExpr;
    }

    public String getReturnJson() {
        return returnJson;
    }

    public void setReturnJson(String returnJson) {
        this.returnJson = returnJson;
    }

    @Override
    public String toString() {
        return "ReturnMock{" +
                "testExpr='" + testExpr + '\'' +
                ", returnJson='" + returnJson + '\'' +
                '}';
    }
}
