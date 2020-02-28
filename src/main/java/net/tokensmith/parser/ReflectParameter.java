package net.tokensmith.parser;

import net.tokensmith.parser.builder.ReflectionBuilder;
import net.tokensmith.parser.exception.ReflectException;
import net.tokensmith.parser.validator.RawType;

import javax.management.ReflectionException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ReflectParameter {
    private Map<String, Function<String, Object>> builders;

    public ReflectParameter(Map<String, Function<String, Object>> builders) {
        this.builders = builders;
    }

    public List<ParamEntity> reflect(Class clazz) throws ReflectException {
        List<ParamEntity> fields = new ArrayList<>();

        if (clazz.getSuperclass() != null) {
            fields = reflect(clazz.getSuperclass());
        }

        for(Field field: clazz.getDeclaredFields()) {
            Parameter p = field.getAnnotation(Parameter.class);
            if (p != null) {
                field.setAccessible(true);

                List<ParamEntity> children = new ArrayList<>();

                ParamEntity node;
                Class<?> nodeClazz;
                if (isParameterized(field)) {
                    ParameterizedType pt = (ParameterizedType) field.getGenericType();
                    String rawType = pt.getRawType().getTypeName();
                    String argType = pt.getActualTypeArguments()[0].getTypeName();
                    Boolean isList = RawType.LIST.getTypeName().equals(rawType);
                    Boolean isOptional = RawType.OPTIONAL.getTypeName().equals(rawType);
                    Function<String, Object> builder = s -> null;
                    if (!p.nested()) {
                        // only need builders for non nested items, b/c parser will use their default.
                        builder = builderForField(argType);
                    }
                    nodeClazz = makeClazz(argType);

                    node = new ParamEntity.Builder()
                            .field(field)
                            .parameter(p)
                            .parameterized(true)
                            .rawType(rawType)
                            .argType(argType)
                            .clazz(nodeClazz)
                            .list(isList)
                            .optional(isOptional)
                            .builder(builder)
                            .children(children)
                            .build();
                } else {
                    Function<String, Object> builder = s -> null;
                    if (!p.nested()) {
                        // only need builders for non nested items, b/c parser will use their default.
                        builder = builderForField(field.getGenericType().getTypeName());
                    }
                    nodeClazz = makeClazz(field.getGenericType().getTypeName());

                    node = new ParamEntity.Builder()
                            .field(field)
                            .parameter(p)
                            .clazz(nodeClazz)
                            .builder(builder)
                            .children(children)
                            .build();
                }

                if (nodeClazz != null && p.nested()){
                    children = reflect(nodeClazz);
                }

                node.setChildren(children);

                fields.add(node);
            }
        }
        return fields;
    }

    protected Class<?> makeClazz(String className) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return clazz;
    }

    protected Boolean isParameterized(Field field) {
        return (field.getGenericType() instanceof ParameterizedType);
    }

    protected Function<String, Object> builderForField(String className) throws ReflectException {
        Function<String, Object> builder = builders.get(className);
        if (builder == null) {
            Constructor<?> ctor = ctorForField(className);
            builder = new ReflectionBuilder(ctor);
            builders.put(className, builder);
        }
        return builder;
    }

    protected Constructor<?> ctorForField(String className) throws ReflectException {
        Class<?> target;
        try {
            target = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ReflectException(String.format("Unable to find class for %s", className), e, className);
        }

        Constructor<?> ctor = null;
        try {
            ctor = target.getConstructor(String.class);
        } catch (NoSuchMethodException e) {
            throw new ReflectException(String.format("Unable to find constructor with String parameter for %s", className), e, className);
        }
        return ctor;
    }

}
