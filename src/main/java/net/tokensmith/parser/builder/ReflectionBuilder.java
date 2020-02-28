package net.tokensmith.parser.builder;

import net.tokensmith.parser.builder.exception.ConstructException;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

public class ReflectionBuilder implements Function<String, Object> {

    private Constructor<?> ctor;

    public ReflectionBuilder(Constructor<?> ctor) {
        this.ctor = ctor;
    }

    @Override
    public Object apply(String s) {
        Object item;
        try {
            item = ctor.newInstance(s);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new ConstructException("Unable to construct with value, " + s, e, s);
        }
        return item;
    }
}
