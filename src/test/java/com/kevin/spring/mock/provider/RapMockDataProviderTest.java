package com.kevin.spring.mock.provider;

import com.google.common.collect.Lists;
import com.kevin.spring.mock.service.IMockTestService;
import com.kevin.spring.mock.domain.Operator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * Nothing seek, nothing find.
 * User:    chenshixin
 * Date:    17/1/12
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext-mock.xml"})
public class RapMockDataProviderTest {
    @Autowired
    private IMockTestService myMockTestService;

    @Test
    public void test() {
        Operator operator = new Operator();
        operator.setId(2001);
        operator.setName("kevin");
        operator.setAddress("Beijing");
        List<Integer> idList = myMockTestService.getMockIdList(Lists.<Integer>newArrayList(2001, 2002), operator);
        System.out.println("idList = " + idList);
    }

}
