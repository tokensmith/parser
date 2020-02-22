package net.tokensmith.parser.factory.simple;

import net.tokensmith.parser.ParamEntity;
import net.tokensmith.parser.exception.OptionalException;
import net.tokensmith.parser.exception.ParseException;
import net.tokensmith.parser.exception.RequiredException;

import java.util.List;

public class ReferenceTypeNullParser implements TypeParser {
    private static String FIELD_ERROR = "Could not set field value";

    @Override
    public <T> void parse(T to, ParamEntity toField, List<String> from) throws ParseException, RequiredException, OptionalException {
        try {
            toField.getField().set(to, null);
        } catch (IllegalAccessException e) {
            throw new ParseException(FIELD_ERROR, e);
        }
    }
}
