package net.tokensmith.parser.validator;



import net.tokensmith.parser.validator.exception.EmptyValueError;
import net.tokensmith.parser.validator.exception.MoreThanOneItemError;

import java.util.List;



public class OptionalParam {
    private static String EMPTY= "parameter is empty";
    private static String TOO_MANY_VALUES= "parameter is empty";

    /**
     * Determines if the items passes the rules for a optional field. Which are:
     *  - items first element cannot be empty (implies an empty value, it must have a non empty value)
     *  - items cannot have more than one value. @see <a href="https://tools.ietf.org/html/rfc6749#section-4.1.2.1">OAuth2 4.1.2.1</a>
     *
     * @param items the input to validate
     * @param allowMany if many items are allowed in the input
     * @return true if passes. raises exception if fails.
     * @throws EmptyValueError if items contains an empty string value
     * @throws MoreThanOneItemError if items has more than one item.
     */
    public boolean run(List<String> items, boolean allowMany) throws EmptyValueError, MoreThanOneItemError {

        // optional parameter.
        if( items == null || items.size() == 0 ) {
            return true;
        }

        if(items.get(0).isEmpty()) {
            throw new EmptyValueError(EMPTY);
        }

        if(!allowMany && items.size() > 1) {
            throw new MoreThanOneItemError(TOO_MANY_VALUES);
        }

        return true;
    }
}
