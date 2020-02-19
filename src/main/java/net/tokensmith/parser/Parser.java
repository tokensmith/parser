package net.tokensmith.parser;


import net.tokensmith.parser.builder.ReflectionBuilder;
import net.tokensmith.parser.exception.OptionalException;
import net.tokensmith.parser.exception.ParseException;
import net.tokensmith.parser.exception.RequiredException;
import net.tokensmith.parser.factory.TypeParser;
import net.tokensmith.parser.factory.TypeParserFactory;
import net.tokensmith.parser.validator.OptionalParam;
import net.tokensmith.parser.validator.RawType;
import net.tokensmith.parser.validator.RequiredParam;
import net.tokensmith.parser.validator.exception.EmptyValueError;
import net.tokensmith.parser.validator.exception.MoreThanOneItemError;
import net.tokensmith.parser.validator.exception.NoItemsError;
import net.tokensmith.parser.validator.exception.ParamIsNullError;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


public class Parser<T extends Parsable> {
    private static String TO_OBJ_ERROR = "Could not construct to object";
    private static String REQ_ERROR="Required field failed validation";
    private static String OPT_ERROR="Optional field failed validation";
    private OptionalParam optionalParam;
    private RequiredParam requiredParam;
    private TypeParserFactory<T> typeParserFactory;
    private Map<String, Function<String, Object>> builders;


    public Parser(OptionalParam optionalParam, RequiredParam requiredParam, TypeParserFactory<T> typeParserFactory, Map<String, Function<String, Object>> builders) {
        this.optionalParam = optionalParam;
        this.requiredParam = requiredParam;
        this.typeParserFactory = typeParserFactory;
        this.builders = builders;
    }

    /**
     * Translates params to an instance of clazz. The definition of class must be annotated
     * with @Parameter on the field level.
     *
     * @param clazz
     * @param fields
     * @param params
     * @return a new instance of clazz
     * @throws RequiredException
     * @throws OptionalException
     * @throws ParseException
     */
    public T to(Class<T> clazz, List<ParamEntity> fields, Map<String, List<String>> params) throws RequiredException, OptionalException, ParseException {

        T to;
        try {
            to = clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new ParseException(TO_OBJ_ERROR, e);
        }

        for(ParamEntity field: fields) {
            // instance variable
            Field f = field.getField();

            // parameter settings for the ivar
            Parameter p = field.getParameter();

            // key to use to assign values to f.
            List<String> from = params.get(p.name());

            try {
                validate(f.getName(), p.name(), from, p.required());
            } catch (OptionalException e) {
                e.setTarget(to);
                throw e;
            } catch (RequiredException e) {
                e.setTarget(to);
                throw e;
            }

            Boolean inputEmpty = isEmpty(from);
            TypeParser<T> parser = typeParserFactory.make(field, inputEmpty);
            parser.parse(to, field, from);
        }
        return to;
    }

    public boolean validate(String field, String param, List<String> input, boolean required) throws RequiredException, OptionalException {
        boolean validated;
        if (required) {
            try {
                validated = requiredParam.run(input);
            } catch (EmptyValueError | MoreThanOneItemError | NoItemsError | ParamIsNullError e) {
                throw new RequiredException(REQ_ERROR, e, field, param);
            }
        } else {
            try {
                validated = optionalParam.run(input);
            } catch (EmptyValueError | MoreThanOneItemError e) {
                throw new OptionalException(OPT_ERROR, e, field, param);
            }
        }
        return validated;
    }

    protected Boolean isEmpty(List<String> values) {
        return values == null || values.size() == 0;
    }

    public List<ParamEntity> reflect(Class clazz) {
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
                String className;
                if (isParameterized(field)) {
                    ParameterizedType pt = (ParameterizedType) field.getGenericType();
                    String rawType = pt.getRawType().getTypeName();
                    String argType = pt.getActualTypeArguments()[0].getTypeName();
                    Boolean isList = RawType.LIST.getTypeName().equals(rawType);
                    Boolean isOptional = RawType.OPTIONAL.getTypeName().equals(rawType);
                    Function<String, Object> builder = builderForField(argType);
                    className = argType;
                    node = new ParamEntity(field, p, true, rawType, argType, isList, isOptional, builder, children);
                } else {
                    Function<String, Object> builder = builderForField(field.getGenericType().getTypeName());
                    className = field.getGenericType().getTypeName();
                    node = new ParamEntity(field, p, false, builder, children);
                }


                Class<?> nodeClazz = null;
                try {
                    nodeClazz = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                if (nodeClazz != null && Parsable.class.isAssignableFrom(nodeClazz)){
                    children = reflect(nodeClazz);
                }

                node.setChildren(children);

                fields.add(node);
            }
        }

        return fields;
    }

    protected Boolean isParameterized(Field field) {
        return (field.getGenericType() instanceof ParameterizedType);
    }

    protected Function<String, Object> builderForField(String className) {
        Function<String, Object> builder = builders.get(className);
        if (builder == null) {
            Constructor<?> ctor = ctorForField(className);
            builder = new ReflectionBuilder(ctor);
            builders.put(className, builder);
        }
        return builder;
    }

    protected Constructor<?> ctorForField(String className) {
        Class<?> target;
        try {
            target = Class.forName(className);
        } catch (ClassNotFoundException e) {
            // TODO: Throw Exception
            return null;
        }

        Constructor<?> ctor = null;
        try {
            ctor = target.getConstructor(String.class);
        } catch (NoSuchMethodException e) {
            // TODO: log me
        }
        return ctor;
    }
}
