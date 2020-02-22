package net.tokensmith.parser;


import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Function;


public class ParamEntity {
    private Field field;
    private Parameter parameter;
    private Boolean parameterized; // indicates if the field is either optional or list
    private String rawType; // parameterized type, java.util.List
    private String argType; // type of individual item List<X> or just X.
    private Class<?> clazz;
    private Boolean isList;
    private Boolean isOptional;
    private Function<String, Object> builder;
    private List<ParamEntity> children;


    public ParamEntity(Field field, Parameter parameter, Boolean isParameterized, Class<?> clazz, Function<String, Object> builder, List<ParamEntity> children) {
        this.field = field;
        this.parameter = parameter;
        this.parameterized = isParameterized;
        this.builder = builder;
        this.children = children;
        this.clazz = clazz;
    }

    public ParamEntity(Field field, Parameter parameter, Boolean parameterized, String rawType, String argType, Class<?> clazz, Boolean isList, Boolean isOptional, Function<String, Object> builder, List<ParamEntity> children) {
        this.field = field;
        this.parameter = parameter;
        this.parameterized = parameterized;
        this.rawType = rawType;
        this.argType = argType;
        this.clazz = clazz;
        this.isList = isList;
        this.isOptional = isOptional;
        this.builder = builder;
        this.children = children;
    }



    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    public Boolean isParameterized() {
        return parameterized;
    }

    public void setParameterized(Boolean parameterized) {
        this.parameterized = parameterized;
    }

    public String getRawType() {
        return rawType;
    }

    public void setRawType(String rawType) {
        this.rawType = rawType;
    }

    public String getArgType() {
        return argType;
    }

    public void setArgType(String argType) {
        this.argType = argType;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Boolean isList() {
        return isList;
    }

    public void setList(Boolean list) {
        isList = list;
    }

    public Boolean isOptional() {
        return isOptional;
    }

    public void setOptional(Boolean optional) {
        isOptional = optional;
    }

    public Function<String, Object> getBuilder() {
        return builder;
    }

    public void setBuilder(Function<String, Object> builder) {
        this.builder = builder;
    }

    public List<ParamEntity> getChildren() {
        return children;
    }

    public void setChildren(List<ParamEntity> children) {
        this.children = children;
    }

    public static class Builder {
        private Field field;
        private Parameter parameter;
        private Boolean parameterized = false; // indicates if the field is either optional or list
        private String rawType; // parameterized type, java.util.List
        private String argType; // type of individual item List<X> or just X.
        private Class<?> clazz;
        private Boolean isList = false;
        private Boolean isOptional = false;
        private Function<String, Object> builder;
        private List<ParamEntity> children;

        public Builder field(Field field) {
            this.field = field;
            return this;
        }

        public Builder parameter(Parameter parameter) {
            this.parameter = parameter;
            return this;
        }

        public Builder parameterized(Boolean parameterized) {
            this.parameterized = parameterized;
            return this;
        }

        public Builder rawType(String rawType) {
            this.rawType = rawType;
            return this;
        }

        public Builder argType(String argType) {
            this.argType = argType;
            return this;
        }

        public Builder clazz(Class<?> clazz) {
            this.clazz = clazz;
            return this;
        }

        public Builder list(Boolean list) {
            isList = list;
            return this;
        }

        public Builder optional(Boolean optional) {
            isOptional = optional;
            return this;
        }

        public Builder builder(Function<String, Object> builder) {
            this.builder = builder;
            return this;
        }

        public Builder children(List<ParamEntity> children) {
            this.children = children;
            return this;
        }

        public ParamEntity build() {
            return new ParamEntity(
                field, parameter, parameterized, rawType, argType, clazz, isList, isOptional, builder, children
            );
        }
    }
}
