package net.tokensmith.parser;



import net.tokensmith.parser.exception.DataTypeException;
import net.tokensmith.parser.exception.OptionalException;
import net.tokensmith.parser.exception.ParseException;
import net.tokensmith.parser.exception.RequiredException;
import net.tokensmith.parser.exception.ValueException;
import net.tokensmith.parser.factory.TypeParser;
import net.tokensmith.parser.factory.TypeParserFactory;
import net.tokensmith.parser.validator.OptionalParam;
import net.tokensmith.parser.validator.RawType;
import net.tokensmith.parser.validator.RequiredParam;
import net.tokensmith.parser.validator.SupportedTypes;
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
import java.util.Optional;
import java.util.UUID;


public class Parser<T> {
    private static String TO_OBJ_ERROR = "Could not construct to object";
    private static String FIELD_ERROR = "Could not set field value";
    private static String CNF_ERROR = "Could not find target class";
    private static String CONSTRUCT_ERROR = "Could not construct field object";;
    private static String REQ_ERROR="Required field failed validation";
    private static String OPT_ERROR="Optional field failed validation";
    private static String DELIMITTER = " ";
    private static String UNSUPPORTED_ERROR = "input value is not supported";
    private OptionalParam optionalParam;
    private RequiredParam requiredParam;
    private TypeParserFactory<T> typeParserFactory;


    public Parser(OptionalParam optionalParam, RequiredParam requiredParam, TypeParserFactory<T> typeParserFactory) {
        this.optionalParam = optionalParam;
        this.requiredParam = requiredParam;
        this.typeParserFactory = typeParserFactory;
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

                if (isParameterized(field)) {
                    ParameterizedType pt = (ParameterizedType) field.getGenericType();
                    String rawType = pt.getRawType().getTypeName();
                    String argType = pt.getActualTypeArguments()[0].getTypeName();
                    Boolean isList = RawType.LIST.getTypeName().equals(rawType);
                    Boolean isOptional = RawType.OPTIONAL.getTypeName().equals(rawType);

                    fields.add(new ParamEntity(field, p, true, rawType, argType, isList, isOptional));
                } else {
                    fields.add(new ParamEntity(field, p, false));
                }

            }
        }

        return fields;
    }

    protected Boolean isParameterized(Field field) {
        return (field.getGenericType() instanceof ParameterizedType);
    }
}
