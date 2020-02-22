package net.tokensmith.parser.factory.nested;

import net.tokensmith.parser.ParamEntity;
import net.tokensmith.parser.exception.ParseException;



public interface NestedTypeSetter {
    <T> void set(T to, ParamEntity toField, Object o) throws ParseException;
}
