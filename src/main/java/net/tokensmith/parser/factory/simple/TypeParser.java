package net.tokensmith.parser.factory.simple;

import net.tokensmith.parser.ParamEntity;
import net.tokensmith.parser.exception.OptionalException;
import net.tokensmith.parser.exception.ParseException;
import net.tokensmith.parser.exception.RequiredException;

import java.util.List;


public interface TypeParser {
    <T> void parse(T to, ParamEntity toField, List<String> from) throws ParseException, RequiredException, OptionalException;
}
