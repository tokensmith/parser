package net.tokensmith.parser;

import java.lang.reflect.Field;


public class ParamEntity {
    private Field field;
    private Parameter parameter;
    private Boolean parameterized; // indicates if the field is either optional or list
    private String rawType; // parameterized type, java.util.List
    private String argType; // type of individual item List<X> or just X.
    private Boolean isList;
    private Boolean isOptional;


    public ParamEntity(Field field, Parameter parameter, Boolean isParameterized) {
        this.field = field;
        this.parameter = parameter;
        this.parameterized = isParameterized;
    }

    public ParamEntity(Field field, Parameter parameter, Boolean parameterized, String rawType, String argType, Boolean isList, Boolean isOptional) {
        this.field = field;
        this.parameter = parameter;
        this.parameterized = parameterized;
        this.rawType = rawType;
        this.argType = argType;
        this.isList = isList;
        this.isOptional = isOptional;
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
}
