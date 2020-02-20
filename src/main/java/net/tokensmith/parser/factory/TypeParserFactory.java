package net.tokensmith.parser.factory;

import net.tokensmith.parser.ParamEntity;
import net.tokensmith.parser.ParserUtils;

public class TypeParserFactory<T> {

    public TypeParser<T> make(ParamEntity toField, Boolean inputEmpty, Boolean required) {
        if(toField.isParameterized() && inputEmpty && toField.isList()) {
            return new EmptyListParser<T>();
        } else if (toField.isParameterized() && inputEmpty && toField.isOptional()) {
            return new EmptyOptionalParser<T>();
        } else if (toField.isParameterized() && !inputEmpty && toField.isList()) {
            return new ListParser<T>(new ParserUtils());
        } else if (toField.isParameterized() && !inputEmpty && toField.isOptional()) {
            return new OptionalParser<T>(new ParserUtils());
        } else if (!inputEmpty && required) {
            return new ReferenceTypeParser<T>(new ParserUtils());
        } else {
            return new ReferenceTypeNullParser<T>();
        }
    }
}
