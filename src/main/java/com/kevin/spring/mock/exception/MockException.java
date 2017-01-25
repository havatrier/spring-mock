package com.kevin.spring.mock.exception;

/**
 * Created by shuchuanjun on 17/1/6.
 */
public class MockException extends Exception {
    public MockException() {
    }

    public MockException(String message) {
        super(message);
    }

    public MockException(String message, Throwable cause) {
        super(message, cause);
    }

    public MockException(Throwable cause) {
        super(cause);
    }

    public MockException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
