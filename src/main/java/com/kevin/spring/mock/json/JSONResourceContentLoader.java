package com.kevin.spring.mock.json;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;

/**
 * load JSON (template) from file resource
 * <p>
 * Created by shuchuanjun on 17/1/6.
 */
public class JSONResourceContentLoader {
    private ResourceLoader resourceLoader;

    public JSONResourceContentLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public String load(String location) throws IOException {
        Resource resource = resourceLoader.getResource(location);
        return IOUtils.toString(resource.getInputStream());
    }
}
