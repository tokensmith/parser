package net.tokensmith.parser;

import net.tokensmith.parser.builder.exception.ConstructException;
import net.tokensmith.parser.factory.nested.NestedTypeSetterFactory;
import net.tokensmith.parser.factory.simple.TypeParserFactory;
import net.tokensmith.parser.graph.GraphTranslator;
import net.tokensmith.parser.validator.OptionalParam;
import net.tokensmith.parser.validator.RequiredParam;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class ParserConfig {

    public Map<String, Function<String, Object>> builders() {
        Map<String, Function<String, Object>> builders = new HashMap<>();
        builders.put("java.util.UUID", s -> {
            try{
                return UUID.fromString(s);
            } catch (Exception e) {
                throw new ConstructException("Unable to construct UUID", e, s);
            }
        });
        return builders;
    }

    public ReflectParameter reflectParameter(Map<String, Function<String, Object>> builders) {
        return new ReflectParameter(builders);
    }

    public Parser parser() {
        Map<String, Function<String, Object>> builders = builders();
        return new Parser(
            reflectParameter(builders),
            new GraphTranslator(),
            new OptionalParam(),
            new RequiredParam(),
            new TypeParserFactory(),
            new NestedTypeSetterFactory()
        );
    }

    public Parser parser(Map<String, Function<String, Object>> builders) {
        Map<String, Function<String, Object>> defaultBuilders = builders();
        defaultBuilders.putAll(builders);

        return new Parser(
                reflectParameter(defaultBuilders),
                new GraphTranslator(),
                new OptionalParam(),
                new RequiredParam(),
                new TypeParserFactory(),
                new NestedTypeSetterFactory()
        );
    }
}
