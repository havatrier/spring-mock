package com.kevin.spring.mock.service;

import com.kevin.spring.mock.domain.Operator;

import java.util.List;

/**
 * Created by shuchuanjun on 17/1/7.
 */
public class MyMockTestService implements IMockTestService {
    private String name = "Hello Test";
    @Override
    public List<Integer> getMockIdList(List<Integer> userIds, Operator operator) {
        return null;
    }
}
