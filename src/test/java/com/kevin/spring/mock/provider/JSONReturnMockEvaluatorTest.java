package com.kevin.spring.mock.provider;

import com.alibaba.fastjson.JSON;
import com.kevin.spring.mock.cfg.JavaType;
import com.kevin.spring.mock.evaluator.JSONReturnMockEvaluator;
import com.kevin.spring.mock.exception.MockRuntimeException;
import com.kevin.spring.mock.domain.Operator;
import com.kevin.spring.mock.domain.Result;
import com.kevin.spring.mock.json.JSONResourceContentLoader;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by shuchuanjun on 17/1/9.
 */
public class JSONReturnMockEvaluatorTest {
    private JSONReturnMockEvaluator evaluator = new JSONReturnMockEvaluator();
    private JSONResourceContentLoader loader = new JSONResourceContentLoader(new DefaultResourceLoader());
    @Test
    public void castJSON() throws ClassNotFoundException, IOException {
        // List - 1
        System.out.println(">>>   List - 1:");
        System.out.println("type : List<Map<Integer, Operator>>");
        JavaType type = JavaType.forName("List<Map<Integer, com.sankuai.meituan.kuailv.util.mtmock.domain.Operator>>");
        JSON json = loadJSONFromFile("list-1.json");
        Object object = evaluator.castJSON(type, json);
        List<Map<Integer, Operator>> list = (List<Map<Integer, Operator>>) object;
        assertTrue(list.size() > 0);
        Operator operator = list.get(0).entrySet().iterator().next().getValue();
        System.out.println("object = " + object);
        System.out.println("operator = " + operator);

        // List - 0 : bad case
        System.out.println("\n>>>   List - 0:");
        System.out.println("type : List<Map<Integer, Operator>>");
        try {
            json = loadJSONFromFile("list-0.json");
            object = evaluator.castJSON(type, json);
            list = (List<Map<Integer, Operator>>) object;
            assertTrue(list.size() > 0);
            operator = list.get(0).entrySet().iterator().next().getValue();
            System.out.println("object = " + object);
            System.out.println("operator = " + operator);
            throw new IllegalStateException();
        } catch (MockRuntimeException e) {
            System.out.println("pass List - 0 test");
        }

        // Set - 1
        System.out.println("\n>>>   Set - 1:");
        System.out.println("type : Set<List<Map<Long, Operator>>>");
        type = JavaType.forName("Set<List<Map<Long, com.sankuai.meituan.kuailv.util.mtmock.domain.Operator>>>");
        json = loadJSONFromFile("set-1.json");
        object = evaluator.castJSON(type, json);
        Set<List<Map<Long, Operator>>> set = (Set<List<Map<Long, Operator>>>) object;
        assertTrue(set.size() > 0);
        operator = set.iterator().next().get(0).entrySet().iterator().next().getValue();
        System.out.println("set = " + set);
        System.out.println("operator = " + operator);

        // Map - 1
        System.out.println("\n>>>   Map - 1 :");
        System.out.println("type : Map<Long, List<Set<Operator>>>");
        type = JavaType.forName("Map<Long, List<Set<com.sankuai.meituan.kuailv.util.mtmock.domain.Operator>>>");
        json = loadJSONFromFile("map-1.json");
        object = evaluator.castJSON(type, json);
        Map<Long, List<Set<Operator>>> map = (Map<Long, List<Set<Operator>>>) object;
        assertTrue(map.size() > 0);
        operator = map.entrySet().iterator().next().getValue().get(0).iterator().next();
        System.out.println("map = " + map);
        System.out.println("operator = " + operator);

        // Collection -1
        System.out.println(">>>   Collection - 1:");
        System.out.println("type : Collection<Map<Integer, Operator>>");
        type = JavaType.forName("Collection<Map<Integer, com.sankuai.meituan.kuailv.util.mtmock.domain.Operator>>");
        json = loadJSONFromFile("list-1.json");
        object = evaluator.castJSON(type, json);
        Collection<Map<Integer, Operator>> collection = (Collection<Map<Integer, Operator>>) object;
        assertTrue(collection.size() > 0);
        operator = collection.iterator().next().entrySet().iterator().next().getValue();
        System.out.println("collection = " + collection);
        System.out.println("operator = " + operator);

        // Generic - 1
        System.out.println(">>>   Generic - 1:");
        System.out.println("type : List<Map<Integer, Result<List<Operator>>>>");
        type = JavaType.forName("List<Map<Integer, com.sankuai.meituan.kuailv.util.mtmock.domain.Result<List<com.sankuai.meituan.kuailv.util.mtmock.domain.Operator>>>>");
        json = loadJSONFromFile("generic-1.json");
        object = evaluator.castJSON(type, json);
        List<Map<Integer, Result<List<Operator>>>> genericList = (List<Map<Integer, Result<List<Operator>>>>) object;
        assertTrue(genericList.size() > 0);
        operator = genericList.get(0).entrySet().iterator().next().getValue().getData().get(0);
        System.out.println("genericList = " + genericList);
        System.out.println("operator = " + operator);


    }

    private JSON loadJSONFromFile(String fileName) throws IOException {
        String location = "classpath:mock-json/" + fileName;
        String content = loader.load(location);
        return (JSON) JSON.parse(content);
    }
}
