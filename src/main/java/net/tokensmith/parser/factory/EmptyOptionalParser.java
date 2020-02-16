package net.tokensmith.parser.factory;

import net.tokensmith.parser.ParamEntity;
import net.tokensmith.parser.exception.ParseException;

import java.util.List;
import java.util.Optional;

public class EmptyOptionalParser<T> implements TypeParser<T> {
    private static String FIELD_ERROR = "Could not set field value";

    @Override
    public void parse(T to, ParamEntity toField, List<String> from) throws ParseException {
        try {
            toField.getField().set(to, Optional.empty());
        } catch (IllegalAccessException e) {
            throw new ParseException(FIELD_ERROR, e);
        }
    }
}
