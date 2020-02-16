package net.tokensmith.parser;

import net.tokensmith.parser.exception.DataTypeException;
import net.tokensmith.parser.exception.OptionalException;
import net.tokensmith.parser.exception.ParseException;
import net.tokensmith.parser.exception.RequiredException;
import net.tokensmith.parser.validator.SupportedTypes;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ParserUtils {
    private static String DELIMITTER = " ";
    private static String CNF_ERROR = "Could not find target class";
    private static String CONSTRUCT_ERROR = "Could not construct field object";
    private static String REQ_ERROR="Required field failed validation";
    private static String OPT_ERROR="Optional field failed validation";

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
        list.addAll(Arrays.asList(items.split(DELIMITTER)));
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
        } catch (IllegalAccessException|InstantiationException| InvocationTargetException e) {
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

    public <T> void handleDataTypeException(DataTypeException e, ParamEntity toField, T o) throws RequiredException, OptionalException {
        e.setField(toField.getField().getName());
        e.setParam(toField.getParameter().name());

        if (toField.getParameter().required()) {
            throw new RequiredException(REQ_ERROR, e, toField.getField().getName(), toField.getParameter().name(), o);
        }
        throw new OptionalException(OPT_ERROR, e, toField.getField().getName(), toField.getParameter().name(), o);
    }
}
