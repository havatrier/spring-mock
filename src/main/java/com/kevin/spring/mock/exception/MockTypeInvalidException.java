package com.kevin.spring.mock.exception;

/**
 * Created by shuchuanjun on 17/1/6.
 */
public class MockTypeInvalidException extends ClassNotFoundException {
    public MockTypeInvalidException() {
    }

    public MockTypeInvalidException(String name) {
        super("Invalid type " + name);
    }

    public MockTypeInvalidException(String name, Throwable ex) {
        super("Invalid type " + name, ex);
    }
}
