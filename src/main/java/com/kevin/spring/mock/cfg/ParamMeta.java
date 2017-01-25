package com.kevin.spring.mock.cfg;

import com.google.common.base.Objects;

/**
 * method parameter definition class
 * <p>Created by shuchuanjun on 17/1/6.
 */
public class ParamMeta {
    private String name; // parameter name
    private JavaType type; // parameter type

    public ParamMeta(String name, JavaType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JavaType getType() {
        return type;
    }

    public void setType(JavaType type) {
        this.type = type;
    }

    public boolean matches(Class<?> cls) {
        return type.isAssignableFrom(cls);
    }

    @Override
    public String toString() {
        return "ParamMeta{" +
                "name='" + name + '\'' +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParamMeta paramMeta = (ParamMeta) o;
        return Objects.equal(name, paramMeta.name) &&
                Objects.equal(type, paramMeta.type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, type);
    }
}
