package net.tokensmith.parser.factory.simple;

import net.tokensmith.parser.ParamEntity;
import net.tokensmith.parser.ParserUtils;
import net.tokensmith.parser.exception.OptionalException;
import net.tokensmith.parser.exception.ParseException;
import net.tokensmith.parser.exception.RequiredException;
import net.tokensmith.parser.exception.ValueException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ListParser implements TypeParser {
    private static String FIELD_ERROR = "Could not set field value";
    private String UNSUPPORTED_ERROR = "input value is not supported";
    private ParserUtils parserUtils;

    public ListParser(ParserUtils parserUtils) {
        this.parserUtils = parserUtils;
    }

    @Override
    public <T> void parse(T to, ParamEntity toField, List<String> from) throws ParseException, RequiredException, OptionalException {
        List<String> fromParsed = new ArrayList<>();
        if (toField.getParameter().parsable()) {
            for(String fromValue: from) {
                fromParsed.addAll(parserUtils.stringToList(fromValue, toField.getParameter().delimiter()));
            }
        } else {
            fromParsed = from;
        }

        Boolean inputOk = parserUtils.isExpected(fromParsed, toField.getParameter().expected());

        if(!inputOk) {
            ValueException ve = new ValueException(UNSUPPORTED_ERROR, toField.getField().getName(), toField.getParameter().name(), from.get(0));
            throw new RequiredException(UNSUPPORTED_ERROR, ve, toField.getField().getName(), toField.getParameter().name(), to);
        }

        ArrayList<Object> arrayList = new ArrayList<>();
        for (String parsedValue : fromParsed) {
            Object item = null;
            try {
                item = toField.getBuilder().apply(parsedValue);
            } catch (Exception e) {
                parserUtils.handleConstructorException(e, toField, to);
            }
            arrayList.add(item);
        }

        try {
            toField.getField().set(to, arrayList);
        } catch (IllegalAccessException e) {
            throw new ParseException(FIELD_ERROR, e);
        }
    }
}
