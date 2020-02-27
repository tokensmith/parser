package net.tokensmith.parser.factory.simple;

import net.tokensmith.parser.ParamEntity;
import net.tokensmith.parser.ParserUtils;
import net.tokensmith.parser.exception.OptionalException;
import net.tokensmith.parser.exception.ParseException;
import net.tokensmith.parser.exception.RequiredException;
import net.tokensmith.parser.exception.ValueException;

import java.util.List;
import java.util.Optional;

public class OptionalParser implements TypeParser {
    private static String FIELD_ERROR = "Could not set field value";
    private String UNSUPPORTED_ERROR = "input value is not supported";
    private ParserUtils parserUtils;

    public OptionalParser(ParserUtils parserUtils) {
        this.parserUtils = parserUtils;
    }

    @Override
    public <T> void parse(T to, ParamEntity toField, List<String> from) throws ParseException, RequiredException, OptionalException {
        Boolean inputOk = parserUtils.isExpected(from.get(0), toField.getParameter().expected());

        if(!inputOk) {
            ValueException ve = new ValueException(UNSUPPORTED_ERROR, toField.getField().getName(), toField.getParameter().name(), from.get(0));
            throw new RequiredException(UNSUPPORTED_ERROR, ve, toField.getField().getName(), toField.getParameter().name(), to);
        }

        Object item = null;
        try {
            item = toField.getBuilder().apply(from.get(0));
        } catch (Exception e) {
            parserUtils.handleConstructorException(e, toField, to);
        }
        try {
            toField.getField().set(to, Optional.of(item));
        } catch (IllegalAccessException e) {
            throw new ParseException(FIELD_ERROR, e);
        }
    }
}
