package net.tokensmith.parser.factory.simple;

import net.tokensmith.parser.ParamEntity;
import net.tokensmith.parser.exception.ParseException;


import java.util.ArrayList;
import java.util.List;

public class EmptyListParser implements TypeParser {
    private static String FIELD_ERROR = "Could not set field value";

    @Override
    public <T> void parse(T to, ParamEntity toField, List<String> from) throws ParseException{
        ArrayList<Object> arrayList = new ArrayList<>();
        try {
            toField.getField().set(to, arrayList);
        } catch (IllegalAccessException e) {
            throw new ParseException(FIELD_ERROR, e);
        }
    }
}
