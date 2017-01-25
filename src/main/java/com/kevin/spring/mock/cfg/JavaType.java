package com.kevin.spring.mock.cfg;

import com.google.common.base.*;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kevin.spring.mock.exception.MockException;
import com.kevin.spring.mock.exception.MockTypeInvalidException;

import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Spring Mock internal Java Type data structure
 * <p>
 * Created by shuchuanjun on 17/1/6.
 */
public class JavaType implements Type {
    private static final String TYPE_REG_EXPR = "([a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z0-9_]+)*)(<(.+)>)?";
    private static final Pattern TYPE_PATTERN = Pattern.compile(TYPE_REG_EXPR);
    private Class<?> rawType; // for "List<String>", rawType = List.class
    private List<JavaType> typeArguments; // for "List<String>",  typeArguments = [String.class]

    private JavaType() {
    }

    public Class<?> getRawType() {
        return rawType;
    }

    public List<JavaType> getTypeArguments() {
        return typeArguments;
    }

    @Override
    public String toString() {
        return "JavaType{" +
                "rawType=" + rawType +
                ", typeArguments=" + typeArguments +
                '}';
    }

    /**
     *  determine whether the type represent by this class is either superclass or same of the cls
     * @param cls
     * @return
     */
    public boolean isAssignableFrom(Class<?> cls) {
        return  rawType.isAssignableFrom(cls);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JavaType javaType = (JavaType) o;
        return com.google.common.base.Objects.equal(rawType, javaType.rawType) &&
                Objects.equal(typeArguments, javaType.typeArguments);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(rawType, typeArguments);
    }

    public static JavaType forName(String name) throws ClassNotFoundException {
        name = name.trim();
        Matcher m = TYPE_PATTERN.matcher(name);
        if (m.matches()) {
            String rawTypeString = m.group(1);
            String typeArgString = m.group(4);

            JavaType paramType = new JavaType();
            paramType.rawType = parseRawType(rawTypeString);

            if (!Strings.isNullOrEmpty(typeArgString)) {
                paramType.typeArguments = Lists.newArrayList();
                try {
                    Iterable<String> argStrings = splitArgumentString(typeArgString);
                    Iterator<String> it = argStrings.iterator();
                    while (it.hasNext()) {
                        String argTypeString = it.next();
                        if (Strings.isNullOrEmpty(argTypeString))
                            throw new MockTypeInvalidException(name);
                        JavaType argType = forName(argTypeString);
                        paramType.typeArguments.add(argType);
                    }
                } catch (MockException e) {
                    throw new MockTypeInvalidException(name, e);
                }
            } else {
                paramType.typeArguments = Collections.emptyList();
            }
            return paramType;
        } else {
            throw new MockTypeInvalidException(name);
        }
    }

    private enum Stat {
        BRACE_START, // brace ('<') starts
        BRACE_END, // brace ('>') ends
        NORMAL; // normal state, i.e characters
    }
    private static List<String> splitArgumentString(String argString) throws MockException {
        List<String> strings = Lists.newArrayList();
        StringBuilder sb = new StringBuilder();
        Stat stat = Stat.NORMAL;
        int bracketLevel = 0;
        for (int i = 0 ; i < argString.length(); i++) {
            char ch = argString.charAt(i);
            switch (ch) {
                case '<':
                    if (stat.equals(Stat.NORMAL) ||
                            stat.equals(Stat.BRACE_START)) {
                        stat = Stat.BRACE_START;
                        sb.append(ch);
                        bracketLevel++;
                    } else {
                        throw new MockException("Invalid parameter type" + argString);
                    }
                    break;
                case '>':
                    if (stat.equals(Stat.BRACE_START)) {
                        bracketLevel--;
                        sb.append(ch);
                        if (bracketLevel == 0) {
                            stat = Stat.BRACE_END;
                        } else if (bracketLevel < 0) {
                            throw new MockException("Invalid parameter type" + argString);
                        }
                    } else {
                        throw new MockException("Invalid parameter type" + argString);
                    }
                    break;
                case ',':
                    if (stat.equals(Stat.NORMAL) ||
                            stat.equals(Stat.BRACE_END)) {
                        stat = Stat.NORMAL;
                        strings.add(sb.toString().trim());
                        sb = new StringBuilder();
                    } else if (stat.equals(Stat.BRACE_START)) {
                        sb.append(ch);
                    } else {
                        throw new MockException("Invalid parameter type" + argString);
                    }
                    break;
                default:
                    if (CharMatcher.WHITESPACE.matches(ch)) {
                        switch (stat) {
                            case NORMAL:
                            case BRACE_START:
                                sb.append(ch);
                                break;
                            case BRACE_END:
                                break;
                        }
                    } else {
                        switch (stat) {
                            case NORMAL:
                            case BRACE_START:
                                sb.append(ch);
                                break;
                            case BRACE_END:
                                throw new MockException("Invalid parameter type" + argString);
                        }
                    }
                    break;
            }
        }

        if (sb.length() > 0) {
            strings.add(sb.toString().trim());
        }
        return strings;
    }

    private static Map<String, Class<?>> fastTypeMap;
    static {
        fastTypeMap = Maps.newHashMap();
        fastTypeMap.put("Map", Map.class);
        fastTypeMap.put("List", List.class);
        fastTypeMap.put("Set", Set.class);
        fastTypeMap.put("Collection", Collection.class);
        fastTypeMap.put("String", String.class);
        fastTypeMap.put("Short", Short.class);
        fastTypeMap.put("Integer", Integer.class);
        fastTypeMap.put("Long", Long.class);
        fastTypeMap.put("Float", Float.class);
        fastTypeMap.put("Double", Double.class);
        fastTypeMap.put("Boolean", Boolean.class);
        fastTypeMap.put("Byte", Byte.class);
        fastTypeMap.put("Object", Object.class);

        fastTypeMap.put("byte", byte.class);
        fastTypeMap.put("short", short.class);
        fastTypeMap.put("int", int.class);
        fastTypeMap.put("long", long.class);
        fastTypeMap.put("float", float.class);
        fastTypeMap.put("double", double.class);
        fastTypeMap.put("boolean", boolean.class);
        fastTypeMap.put("char", char.class);

        fastTypeMap.put("[byte", byte[].class);
        fastTypeMap.put("[short", short[].class);
        fastTypeMap.put("[int", int[].class);
        fastTypeMap.put("[long", long[].class);
        fastTypeMap.put("[float", float[].class);
        fastTypeMap.put("[double", double[].class);
        fastTypeMap.put("[boolean", boolean[].class);
        fastTypeMap.put("[char", char[].class);
    }
    private static Class<?> parseRawType(String rawName) throws ClassNotFoundException {
        if (fastTypeMap.containsKey(rawName))
            return fastTypeMap.get(rawName);
        return Class.forName(rawName);
    }
}
