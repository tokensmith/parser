package net.tokensmith.parser;



import net.tokensmith.parser.exception.DataTypeException;
import net.tokensmith.parser.exception.OptionalException;
import net.tokensmith.parser.exception.ParseException;
import net.tokensmith.parser.exception.RequiredException;
import net.tokensmith.parser.exception.ValueException;
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


    public Parser(OptionalParam optionalParam, RequiredParam requiredParam) {
        this.optionalParam = optionalParam;
        this.requiredParam = requiredParam;
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
        T o;
        try {
            o = clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new ParseException(TO_OBJ_ERROR, e);
        }

        for(ParamEntity field: fields) {
            // instance variable
            Field f = field.getField();

            // parameter settings for the ivar
            Parameter p = field.getParameter();
            String[] expected = p.expected();

            // key to use to assign values to f.
            List<String> from = params.get(p.name());

            try {
                validate(f.getName(), p.name(), from, p.required());
            } catch (OptionalException e) {
                e.setTarget(o);
                throw e;
            } catch (RequiredException e) {
                e.setTarget(o);
                throw e;
            }

            try {
                if(field.isParameterized() && isEmpty(from) && field.isList()) {
                    ArrayList<Object> arrayList = new ArrayList<>();
                    f.set(o, arrayList);
                } else if (field.isParameterized() && isEmpty(from) && field.isOptional()) {
                    f.set(o, Optional.empty());
                } else if (field.isParameterized() && !isEmpty(from)) {
                    // there maybe multiple values in from since it could be a list.
                    List<String> parsedValues = stringToList(from.get(0));
                    Boolean inputOk = isExpected(parsedValues, expected);

                    if (inputOk && field.isList()){
                        ArrayList arrayList = new ArrayList<>();
                        for (String parsedValue : parsedValues) {
                            // this could be a factory with static implementations
                            var item = make(field.getArgType(), parsedValue);
                            arrayList.add(item);
                        }
                        f.set(o, arrayList);
                    } else if (inputOk && field.isOptional()) {
                        Object item = make(field.getArgType(), from.get(0));
                        f.set(o, Optional.of(item));

                    } else {
                        ValueException ve = new ValueException(UNSUPPORTED_ERROR, f.getName(), p.name(), from.get(0));
                        throw new RequiredException(UNSUPPORTED_ERROR, ve, f.getName(), p.name(), o);
                    }

                } else if (isExpected(from.get(0), expected)){
                    Object item = make(f.getGenericType().getTypeName(), from.get(0));
                    f.set(o, item);
                } else {
                    ValueException ve = new ValueException(UNSUPPORTED_ERROR, f.getName(), p.name(), from.get(0));
                    throw new RequiredException(UNSUPPORTED_ERROR, ve, f.getName(), p.name(), o);
                }
            } catch (IllegalAccessException e) {
                // is thrown when setting field from, f.set(o, X)
                throw new ParseException(FIELD_ERROR, e);
            } catch (DataTypeException e) {
                // is thrown from, make(className, input)
                e.setField(f.getName());
                e.setParam(p.name());

                if (p.required()) {
                    throw new RequiredException(REQ_ERROR, e, f.getName(), p.name(), o);
                }
                throw new OptionalException(OPT_ERROR, e, f.getName(), p.name(), o);
            }
        }
        return o;
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

    /**
     * Translate a string that is space delimited into a List of Strings
     * OAuth2 will pass in a space delimited string for a url parameter value
     * That should be parsed into a list.
     *
     * @param items
     * @return
     */
    public List<String> stringToList(String items) {
        List<String> list = new ArrayList<>();
        for(String item: items.split(DELIMITTER)) {
            list.add(item);
        }
        return list;
    }

    public Boolean isExpected(List<String> items, String[] expectedValues) {
        for(String item: items) {
            if (!isExpected(item, expectedValues)) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    public Boolean isExpected(String item, String[] expectedValues) {
        if (expectedValues.length == 0) {
            return true;
        }

        for(String expected: expectedValues) {
            if (expected.toLowerCase().equals(item.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Attempts to translate a String, input, to the desired object type, className.
     *
     * @param className
     * @param input
     * @return a new Object with the type of, className
     * @throws DataTypeException
     * @throws ParseException
     */
    public Object make(String className, String input) throws ParseException, DataTypeException {
        Object item;
        Class target;
        try {
            target = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ParseException(CNF_ERROR, e);
        }

        try {
            Constructor ctor = target.getConstructor(String.class);
            item = ctor.newInstance(input);
        } catch (NoSuchMethodException e) {
            item = makeFromListOfTypes(className, input);
        } catch (IllegalAccessException|InstantiationException|InvocationTargetException e) {
            throw new ParseException(CONSTRUCT_ERROR, e);
        }

        return item;
    }

    public Object makeFromListOfTypes(String className, String input) throws ParseException, DataTypeException {
        Object item;

        try {
            if (SupportedTypes.UUID.getType().equals(className)) {
                item = UUID.fromString(input);
            } else {
                throw new ParseException(CONSTRUCT_ERROR);
            }
        } catch (IllegalArgumentException e) {
            throw new DataTypeException(CONSTRUCT_ERROR, e, input);
        }

        return item;
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
