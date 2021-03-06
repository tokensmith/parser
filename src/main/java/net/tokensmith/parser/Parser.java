package net.tokensmith.parser;



import net.tokensmith.parser.exception.OptionalException;
import net.tokensmith.parser.exception.ParseException;
import net.tokensmith.parser.exception.ReflectException;
import net.tokensmith.parser.exception.RequiredException;
import net.tokensmith.parser.factory.nested.NestedTypeSetter;
import net.tokensmith.parser.factory.nested.NestedTypeSetterFactory;
import net.tokensmith.parser.factory.simple.TypeParser;
import net.tokensmith.parser.factory.simple.TypeParserFactory;
import net.tokensmith.parser.graph.GraphNode;
import net.tokensmith.parser.graph.GraphTranslator;
import net.tokensmith.parser.model.NodeData;
import net.tokensmith.parser.validator.OptionalParam;
import net.tokensmith.parser.validator.RequiredParam;
import net.tokensmith.parser.validator.exception.EmptyValueError;
import net.tokensmith.parser.validator.exception.MoreThanOneItemError;
import net.tokensmith.parser.validator.exception.NoItemsError;
import net.tokensmith.parser.validator.exception.ParamIsNullError;



import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class Parser {
    private static String TO_OBJ_ERROR = "Could not construct to object";
    private static String REQ_ERROR="Required field failed validation";
    private static String OPT_ERROR="Optional field failed validation";
    private static Map<Class<?>, List<ParamEntity>> reflectedFields = new HashMap<>();
    private ReflectParameter reflectParameter;
    private GraphTranslator graphTranslator;
    private OptionalParam optionalParam;
    private RequiredParam requiredParam;
    private TypeParserFactory typeParserFactory;
    private NestedTypeSetterFactory nestedTypeSetterFactory;


    public Parser(ReflectParameter reflectParameter, GraphTranslator graphTranslator, OptionalParam optionalParam, RequiredParam requiredParam, TypeParserFactory typeParserFactory, NestedTypeSetterFactory nestedTypeSetterFactory) {
        this.reflectParameter = reflectParameter;
        this.graphTranslator = graphTranslator;
        this.optionalParam = optionalParam;
        this.requiredParam = requiredParam;
        this.typeParserFactory = typeParserFactory;
        this.nestedTypeSetterFactory = nestedTypeSetterFactory;
    }

    /**
     * Translates from to an instance of T.
     *
     * @param clazz the Class that is returned
     * @param from the data to translate to T
     * @param <T> the type to translate to
     * @return a new instance of T
     * @throws RequiredException if a field that is required is empty, null, or not present
     * @throws OptionalException if a field that is optional is present and empty or null. If its there it should have a value.
     * @throws ParseException if something went wrong in the framework. If a constructor could not be found, field could not be set, etc.
     */
    public <T> T to(Class<T> clazz, Map<String, List<String>> from) throws RequiredException, OptionalException, ParseException {
        List<ParamEntity> fields = reflectedFields.get(clazz);
        if (fields == null) {
            try {
                fields = reflectParameter.reflect(clazz);
            } catch (ReflectException e) {
                throw new ParseException(String.format("problem reflecting class %s", clazz.toString()), e);
            }
            reflectedFields.put(clazz, fields);
        }
        Map<String, GraphNode<NodeData>> fromGraph = graphTranslator.to(from);
        return toFromGraph(clazz, fields, fromGraph);
    }

    /**
     * Translates from to an instance of T.
     *
     * @param clazz the Class that is returned
     * @param fields the List that returned by reflect(Class clazz)
     * @param from the data to translate to T, in the form of a graph.
     * @param <T> the type to translate to
     * @return a new instance of T
     * @throws RequiredException if a field that is required is empty, null, or not present
     * @throws OptionalException if a field that is optional is present and empty or null. If its there it should have a value.
     * @throws ParseException if something went wrong in the framework.
     */
    public <T> T toFromGraph(Class<T> clazz, List<ParamEntity> fields, Map<String, GraphNode<NodeData>> from) throws RequiredException, OptionalException, ParseException {

        T to;
        try {
            to = clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new ParseException(TO_OBJ_ERROR, e);
        }

        for(ParamEntity field: fields) {
            // instance variable
            Field f = field.getField();

            // parameter settings for the ivar
            Parameter p = field.getParameter();

            GraphNode<NodeData> node = from.get(p.name());


            if (field.getChildren().size() > 0) {
                // complex, nested path..
                validateNested(field.getParameter().name(), field, node, to);

                NestedTypeSetter setter = nestedTypeSetterFactory.make(field, node);

                if (node != null) {
                    // this is a required parameter.
                    var item = toFromGraph(field.getClazz(), field.getChildren(), node.getChildren());
                    setter.set(to, field, item);
                } else {
                    // not required
                    setter.set(to, field, null);
                }
            } else {
                // simple, not nested path
                List<String> fromValues = null;
                if (node != null) {
                    fromValues = node.getData().getValues();
                }

                try {
                    validate(f.getName(), p.name(), fromValues, p.required(), p.allowMany());
                } catch (OptionalException e) {
                    e.setTarget(to);
                    throw e;
                } catch (RequiredException e) {
                    e.setTarget(to);
                    throw e;
                }

                Boolean inputEmpty = isEmpty(fromValues);

                TypeParser parser = typeParserFactory.make(field, inputEmpty, p.required());
                parser.parse(to, field, fromValues);
            }
        }
        return to;
    }

    protected boolean validate(String field, String param, List<String> input, boolean required, boolean allowMany) throws RequiredException, OptionalException {
        boolean validated;
        if (required) {
            try {
                validated = requiredParam.run(input, allowMany);
            } catch (EmptyValueError | MoreThanOneItemError | NoItemsError | ParamIsNullError e) {
                throw new RequiredException(REQ_ERROR, e, field, param);
            }
        } else {
            try {
                validated = optionalParam.run(input, allowMany);
            } catch (EmptyValueError | MoreThanOneItemError e) {
                throw new OptionalException(OPT_ERROR, e, field, param);
            }
        }
        return validated;
    }

    protected Boolean isEmpty(List<String> values) {
        return values == null || values.size() == 0;
    }

    // nested rules are less complex so they do not need their own use cases like the others.
    protected <T> void validateNested(String field, ParamEntity paramEntity, GraphNode<NodeData> node, T target) throws RequiredException, OptionalException {
        if (node == null && paramEntity.getParameter().required()) {
            // its missing
            ParamIsNullError cause = new ParamIsNullError(
                    String.format("Param value for, %s, is null", field)
            );
            throw new RequiredException(REQ_ERROR, cause, field, null, target);
        } else if (node != null && node.getChildren().size() == 0 && !paramEntity.getParameter().required() ) {
            // key is present but has values..
            EmptyValueError cause = new EmptyValueError(
                    String.format("Param value for, %s, is not present", field)
            );
            throw new OptionalException("", cause, field, null, target);
        }
    }
}
