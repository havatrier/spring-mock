package com.kevin.spring.mock.evaluator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.FieldDeserializer;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kevin.spring.mock.cfg.JavaType;
import com.kevin.spring.mock.cfg.ParamMeta;
import com.kevin.spring.mock.exception.MockRuntimeException;
import com.kevin.spring.mock.cfg.MethodMockConfig;
import com.kevin.spring.mock.cfg.ReturnMock;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.collections.CollectionUtils;

import java.io.StringWriter;
import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;

import static com.alibaba.fastjson.util.TypeUtils.cast;

/**
 * Parse json (template) of {@link ReturnMock ReturnMock} based on <tt>Freemarker</tt>,
 * and convert the json result to an Object with java type defined by {@link MethodMockConfig#returnType MethodMockConfig's returnType}
 * <p>
 * <p>
 *
 * Created by shuchuanjun on 17/1/8.
 */
public class JSONReturnMockEvaluator implements ReturnMockEvaluator {
    private Configuration freeMarkerConfig;
    private StringTemplateLoader stringTemplateLoader;

    private CastContext castContext;

    public JSONReturnMockEvaluator() {
        stringTemplateLoader = new StringTemplateLoader();
        freeMarkerConfig = new Configuration();
        freeMarkerConfig.setTemplateLoader(stringTemplateLoader);
        freeMarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        castContext = new CastContext();

    }
    @Override
    public Object evaluate(MethodMockConfig methodMock, ReturnMock returnMock, Object[]arguments) {
        String jsonResult  = evaluateJsonResult(methodMock.getParams(), returnMock.getId(), returnMock.getReturnJson(), arguments);
        return castJsonResult(jsonResult, methodMock);
    }

    private Object castJsonResult(String jsonResult, MethodMockConfig methodMock) {
        JavaType returnType = methodMock.getReturnType();
        try {
            JSON json = (JSON) JSON.parse(jsonResult);
            castContext.setContext(methodMock, json);
            return castJSON(returnType, json);
        } catch (ClassCastException ex) {
            throw new MockRuntimeException("Invalid json mock data in " + methodMock, ex);
        }
    }

    public Object castJSON(JavaType type, JSON json) {
        Class<?> rawType = type.getRawType();
        if (List.class.isAssignableFrom(rawType)) {
            if (json instanceof JSONArray) {
                return castToList(type, (JSONArray) json);
            } else {
                throwReturnTypeIncompatibleException();
            }
        } else if (Set.class.isAssignableFrom(rawType)) {
            if (json instanceof JSONArray) {
                return castToSet(type, (JSONArray) json);
            } else {
                throwReturnTypeIncompatibleException();
            }
        } else if (Map.class.isAssignableFrom(rawType)) {
            if (json instanceof JSONObject) {
                return castToMap(type, (JSONObject) json);
            } else {
                throwReturnTypeIncompatibleException();
            }
        } else if (Collection.class.isAssignableFrom(rawType)) {
            if (json instanceof JSONArray) {
                return castToList(type, (JSONArray) json);
            } else {
                throwReturnTypeIncompatibleException();
            }
        } else {
            if (json instanceof JSONObject) {
                if (CollectionUtils.isEmpty(type.getTypeArguments())) { // common java bean class
                    return castToJavaBean(type.getRawType(), (JSONObject) json);
                } else { // parametrized java bean class, e.g. ResultDto<T>
                    try {
                        return castToParametrizedJavaBean(type, (JSONObject) json);
                    } catch (Exception e) {
                        throwReturnTypeIncompatibleException(e);
                    }
                }
            } else {
                throwReturnTypeIncompatibleException();
            }
        }
        throw new MockRuntimeException(castContext.getMethodMock() +
                " incompatible with parsed return mock " + castContext.getJson());
    }

    private Map<?, ?> castToMap(JavaType type, JSONObject jsonObject) {
        Preconditions.checkState(type.getTypeArguments().size() == 2);
        JavaType keyType = type.getTypeArguments().get(0);
        JavaType valType = type.getTypeArguments().get(1);
        Map<Object, Object> map = Maps.newHashMapWithExpectedSize(jsonObject.size());
        if (!jsonObject.isEmpty()) {
            Iterator<Entry<String, Object>> iterator = jsonObject.entrySet().iterator();
            Entry<String, Object> entry = iterator.next();
            String key = entry.getKey();
            Object val = entry.getValue();
            if (val instanceof JSON) {
                map.put(castToLiteralValue(keyType.getRawType(), key),
                        castJSON(valType, (JSON) val));
                while (iterator.hasNext()) {
                    entry = iterator.next();
                    key = entry.getKey();
                    val = entry.getValue();
                    if (val instanceof JSON) {
                        map.put(castToLiteralValue(keyType.getRawType(), key),
                                castJSON(valType, (JSON) val));
                    } else {
                        throwReturnTypeIncompatibleException();
                    }
                }
            } else { // literal value
                map.put(castToLiteralValue(keyType.getRawType(), key),
                        castToLiteralValue(valType.getRawType(), val));
                while (iterator.hasNext()) {
                    entry = iterator.next();
                    key = entry.getKey();
                    val = entry.getValue();
                    if (val instanceof JSON) {
                        throwReturnTypeIncompatibleException();
                    } else {
                        map.put(castToLiteralValue(keyType.getRawType(), key),
                                castToLiteralValue(valType.getRawType(), val));
                    }
                }
            }
        }
        return map;
    }


    private Set<?> castToSet(JavaType type, JSONArray jsonArray) {
        return Sets.newHashSet(castToList(type, jsonArray));
    }

    private List<?> castToList(JavaType type, JSONArray jsonArray) {
        Preconditions.checkState(type.getTypeArguments().size() == 1);
        List<Object> list = Lists.newArrayList();
        JavaType elementType = type.getTypeArguments().get(0);
        if (!jsonArray.isEmpty()) {
            Object e = jsonArray.get(0);
            if (e instanceof JSON) {
                list.add(castJSON(elementType, (JSON) e));
                for (int i = 1; i < jsonArray.size(); i++) {
                    e = jsonArray.get(i);
                    if (e instanceof JSON) {
                        list.add(castJSON(elementType, (JSON) e));
                    } else {
                        throwReturnTypeIncompatibleException();
                    }
                }
            } else { // literal value
                if (CollectionUtils.isNotEmpty(elementType.getTypeArguments())) {
                    throwReturnTypeIncompatibleException();
                }
                list.add(castToLiteralValue(elementType.getRawType(), e));
                for (int i = 1; i < jsonArray.size(); i++) {
                    if (e instanceof JSON) {
                       throwReturnTypeIncompatibleException();
                    } else {
                        list.add(castToLiteralValue(elementType.getRawType(), e));
                    }
                }
            }
        }
        return list;
    }

    private Object castToParametrizedJavaBean(JavaType type, JSONObject jsonObject) throws Exception {
        Class<?> rawType = type.getRawType();
        Preconditions.checkState(rawType.getTypeParameters().length == type.getTypeArguments().size());

        ParserConfig mapping = ParserConfig.getGlobalInstance();
        Map<String, FieldDeserializer> setters = mapping.getFieldDeserializers(rawType);

        Constructor<?> constructor = rawType.getDeclaredConstructor();
        if (!constructor.isAccessible()) {
            constructor.setAccessible(true);
        }
        Object object = constructor.newInstance();

        for (Map.Entry<String, FieldDeserializer> entry : setters.entrySet()) {
            String key = entry.getKey();
            FieldDeserializer fieldDeser = entry.getValue();

            if (jsonObject.containsKey(key)) {
                Object value = jsonObject.get(key);
                Method method = fieldDeser.getMethod();
                if (method != null) {
                    Type paramType = method.getGenericParameterTypes()[0];
                    if (paramType instanceof TypeVariable) {
                        JavaType paramJavaType = resolveFiledType(type, paramType);
                        if (value instanceof JSON) {
                            value = castJSON(paramJavaType, (JSON) value);
                        } else {
                            value = castToLiteralValue(paramJavaType.getRawType(), value);
                        }
                    } else {
                        value = cast(value, paramType, mapping);
                    }
                    method.invoke(object, new Object[] { value });
                } else {
                    Field field = fieldDeser.getField();
                    Type paramType = field.getGenericType();
                    if (paramType instanceof TypeVariable) {
                        JavaType paramJavaType = resolveFiledType(type, paramType);
                        if (value instanceof JSON) {
                            value = castJSON(paramJavaType, (JSON) value);
                        } else {
                            value = castToLiteralValue(paramJavaType.getRawType(), value);
                        }
                    } else {
                        value = cast(value, paramType, mapping);
                    }
                    field.set(object, value);
                }
            }
        }
        return object;
    }

    private Object castToJavaBean(Class<?> clazz, JSONObject jsonObject) {
        return cast(jsonObject, clazz, ParserConfig.getGlobalInstance());
    }
    private Object castToLiteralValue(Class<?> clazz, Object obj) {
        return cast(obj, clazz, ParserConfig.getGlobalInstance());
    }

    /**
     *  resolve the field's JavaType
     * @param javaType parametrized java bean's JavaType
     * @param filedType field type
     * @return
     */
    private JavaType resolveFiledType(JavaType javaType, Type filedType) {
        Type[] types = javaType.getRawType().getTypeParameters();
        for (int i = 0; i < types.length; i++) {
            Type type = types[i];
            if (type == filedType)
                return javaType.getTypeArguments().get(i);
        }
        throwReturnTypeIncompatibleException();
        return null;
    }

    private void throwReturnTypeIncompatibleException(Throwable t) {
        throw new MockRuntimeException(castContext.getMethodMock() +
                " incompatible with parsed return mock " + castContext.getJson(), t);
    }

    private void throwReturnTypeIncompatibleException() {
        throw new MockRuntimeException(castContext.getMethodMock() +
                " incompatible with parsed return mock " + castContext.getJson());
    }


    private String evaluateJsonResult(List<ParamMeta> params, String returnMockId, String jsonTemplate, Object[] arguments) {
        if (stringTemplateLoader.findTemplateSource(returnMockId) == null) {
            stringTemplateLoader.putTemplate(returnMockId, jsonTemplate);
        }
        try {
            StringWriter writer = new StringWriter();
            Template template = freeMarkerConfig.getTemplate(returnMockId);
            template.process(createRootMap(params, arguments), writer);
            return writer.getBuffer().toString();
        } catch (Exception e) {
            throw new MockRuntimeException(e);
        }
    }

    private Map<String, Object> createRootMap(List<ParamMeta> params, Object[] arguments) {
        Preconditions.checkState(params.size() == arguments.length, "Arguments length (" + arguments.length +
                ") does not match params number (" + params.size() + ")");
        Map<String, Object> map = Maps.newHashMap();
        for (int i = 0; i < params.size(); i++) {
            ParamMeta param = params.get(i);
            Object argument = arguments[i];
            map.put(param.getName(), argument);
        }
        return map;
    }

    static class CastContext {
        private MethodMockConfig methodMock;
        private JSON json;

        public void setContext(MethodMockConfig methodMock, JSON json) {
            this.methodMock = methodMock;
            this.json = json;
        }

        public MethodMockConfig getMethodMock() {
            return methodMock;
        }

        public JSON getJson() {
            return json;
        }
    }
}
