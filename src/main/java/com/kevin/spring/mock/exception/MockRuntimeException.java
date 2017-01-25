package com.kevin.spring.mock.exception;

/**
 * Created by shuchuanjun on 17/1/6.
 */
public class MockRuntimeException extends RuntimeException {
    public MockRuntimeException() {
    }

    public MockRuntimeException(String message) {
        super(message);
    }

    public MockRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public MockRuntimeException(Throwable cause) {
        super(cause);
    }

    public MockRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
