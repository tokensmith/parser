package net.tokensmith.parser.factory.nested;

import net.tokensmith.parser.ParamEntity;
import net.tokensmith.parser.exception.ParseException;

import java.util.Optional;

public class OptionalTypeEmptySetter implements NestedTypeSetter {
    private static String FIELD_ERROR = "Could not set field value";

    @Override
    public <T> void set(T to, ParamEntity toField, Object o) throws ParseException {
        try {
            toField.getField().set(to, Optional.empty());
        } catch (IllegalAccessException e) {
            throw new ParseException(FIELD_ERROR, e);
        }
    }
}
