package com.kevin.spring.mock.service;

import com.kevin.spring.mock.domain.Operator;

import java.util.List;

/**
 * Created by shuchuanjun on 17/1/7.
 */
public interface IMockTestService {
    List<Integer> getMockIdList(List<Integer> userIds, Operator operator);
}
