package com.kevin.spring.mock.provider;

import com.kevin.spring.mock.service.IMockTestService;
import com.kevin.spring.mock.cfg.ServiceMockConfig;
import org.junit.Test;

import java.io.InputStream;

/**
 * Created by shuchuanjun on 17/1/7.
 */
public class XmlMockDataProviderTest {
    @Test
    public void test() {
        XmlMockDataProvider provider = new XmlMockDataProvider("mock.xml");
        ServiceMockConfig config = provider.getServiceMockConfig(IMockTestService.class);
        System.out.println("config = " + config);
    }

    @Test
    public void testResource() {
        String path = "mock.xml";
        InputStream is = this.getClass().getResourceAsStream(path);
        System.out.println("is = " + is);
    }
}
