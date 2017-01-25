package com.kevin.spring.mock.cfg;

import org.junit.Test;

/**
 * Created by shuchuanjun on 17/1/6.
 */
public class ParamTypeTest {
    @Test
    public void testForName() throws ClassNotFoundException {
        JavaType type = JavaType.forName("Map<String, List<Integer, String>>");
        System.out.println("type = " + type);

       type = JavaType.forName("com.sankuai.meituan.kuailv.util.config.ConfigUtilAdapterInitializer");
        System.out.println("type = " + type);
    }
}
