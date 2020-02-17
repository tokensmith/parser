package net.tokensmith.parser;

import net.tokensmith.parser.exception.OptionalException;
import net.tokensmith.parser.exception.RequiredException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ParserUtils {
    private static String DELIMITTER = " ";
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
        return new ArrayList<>(Arrays.asList(items.split(DELIMITTER)));
    }

    public Boolean isExpected(List<String> items, String[] expectedValues) {
        return items.stream().anyMatch(a -> isExpected(a, expectedValues));
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

    public <T> void handleConstructorException(Throwable t, ParamEntity toField, T o) throws RequiredException, OptionalException {

        if (toField.getParameter().required()) {
            throw new RequiredException(REQ_ERROR, t, toField.getField().getName(), toField.getParameter().name(), o);
        }
        throw new OptionalException(OPT_ERROR, t, toField.getField().getName(), toField.getParameter().name(), o);
    }
}
