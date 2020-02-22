package net.tokensmith.parser.factory.simple;

import net.tokensmith.parser.ParamEntity;
import net.tokensmith.parser.ParserUtils;

public class TypeParserFactory {

    public TypeParser make(ParamEntity toField, Boolean inputEmpty, Boolean required) {
        if(toField.isParameterized() && inputEmpty && toField.isList()) {
            return new EmptyListParser();
        } else if (toField.isParameterized() && inputEmpty && toField.isOptional()) {
            return new EmptyOptionalParser();
        } else if (toField.isParameterized() && !inputEmpty && toField.isList()) {
            return new ListParser(new ParserUtils());
        } else if (toField.isParameterized() && !inputEmpty && toField.isOptional()) {
            return new OptionalParser(new ParserUtils());
        } else if (!inputEmpty && required) {
            return new ReferenceTypeParser(new ParserUtils());
        } else {
            return new ReferenceTypeNullParser();
        }
    }
}
