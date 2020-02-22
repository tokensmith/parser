package net.tokensmith.parser;

import net.tokensmith.parser.builder.exception.ConstructException;
import net.tokensmith.parser.exception.OptionalException;
import net.tokensmith.parser.exception.RequiredException;
import net.tokensmith.parser.exception.ValueException;
import net.tokensmith.parser.factory.nested.NestedTypeSetterFactory;
import net.tokensmith.parser.factory.simple.TypeParserFactory;
import net.tokensmith.parser.graph.GraphTranslator;
import net.tokensmith.parser.helper.FixtureFactory;
import net.tokensmith.parser.validator.Dummy;
import net.tokensmith.parser.validator.OptionalParam;
import net.tokensmith.parser.validator.RequiredParam;
import net.tokensmith.parser.validator.exception.ParamIsNullError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    private Parser subject;
    private ReflectParameter reflectParameter;

    // names of fields expected
    private List<String> names = Arrays.asList(
            "string", "id", "uri",
            "strings", "ids", "uris",
            "optString", "optId", "optUri",
            "optList", "nested"
    );

    @BeforeEach
    void setUp() {
        Map<String, Function<String, Object>> builders = new HashMap<>();
        builders.put("java.util.UUID", s -> {
            try{
                return UUID.fromString(s);
            } catch (Exception e) {
                throw new ConstructException("", e);
            }
        });
        reflectParameter = new ReflectParameter(builders);
        subject = new Parser(
            new GraphTranslator(), new OptionalParam(), new RequiredParam(), new TypeParserFactory(), new NestedTypeSetterFactory()
        );
    }



    @Test
    public void toShouldTranslate() throws Exception {
        List<ParamEntity> fields = reflectParameter.reflect(Dummy.class);
        Map<String, List<String>> params = FixtureFactory.makeForDummyNestedParameters();

        Dummy actual = subject.to(Dummy.class, fields, params);

        assertNotNull(actual);

        // string
        String expectedString = params.get("string").get(0);
        assertNotNull(actual.getString());
        assertEquals(expectedString, actual.getString());

        assertNotNull(actual.getStrings());
        assertEquals(3, actual.getStrings().size());
        assertEquals("string1", actual.getStrings().get(0));
        assertEquals("string2", actual.getStrings().get(1));
        assertEquals("string3", actual.getStrings().get(2));

        assertNotNull(actual.getOptString());
        assertTrue(actual.getOptString().isPresent());
        assertEquals(expectedString, actual.getOptString().get());

        // id
        UUID expectedId = UUID.fromString(params.get("uuid").get(0));
        assertNotNull(actual.getId());
        assertEquals(expectedId, actual.getId());

        assertNotNull(actual.getIds());
        assertEquals(1, actual.getIds().size());
        assertEquals(expectedId, actual.getIds().get(0));

        assertNotNull(actual.getOptId());
        assertTrue(actual.getOptId().isPresent());
        assertEquals(expectedId, actual.getOptId().get());

        // uri
        URI expectedUri = new URI(params.get("uri").get(0));
        assertNotNull(actual.getUri());
        assertEquals(expectedUri, actual.getUri());

        assertNotNull(actual.getUris());
        assertEquals(1, actual.getUris().size());
        assertEquals(expectedUri, actual.getUris().get(0));

        assertNotNull(actual.getOptUri());
        assertTrue(actual.getOptUri().isPresent());
        assertEquals(expectedUri, actual.getOptUri().get());

        assertNotNull(actual.getOptList());
        assertEquals(1, actual.getOptList().size());
        assertEquals("opt_list1", actual.getOptList().get(0));

        // not annotated field - should not have been assigned.
        assertNull(actual.getNotAnnotated());

        // TODO: need to inspect nested.
    }

    @Test
    public void toWhenTypeOptionalEmptyListShouldTranslate() throws Exception {
        List<ParamEntity> fields = reflectParameter.reflect(Dummy.class);
        Map<String, List<String>> params = FixtureFactory.makeForDummyNestedParameters();
        params.put("opt_uuid", new ArrayList<>());

        Dummy actual = (Dummy) subject.to(Dummy.class, fields, params);

        assertNotNull(actual);
        assertNotNull(actual.getOptId());
        assertFalse(actual.getOptId().isPresent());

    }

    @Test
    public void toWhenTypeListEmptyListShouldTranslate() throws Exception {
        List<ParamEntity> fields = reflectParameter.reflect(Dummy.class);
        Map<String, List<String>> params = FixtureFactory.makeForDummyNestedParameters();
        params.put("opt_list", new ArrayList<>());

        Dummy actual = (Dummy) subject.to(Dummy.class, fields, params);

        assertNotNull(actual);
        assertNotNull(actual.getOptList());
        assertEquals(actual.getOptList().size(), 0);

    }

    @Test
    public void toWhenOptFieldIsMissingShouldMakeAEmptyValue() throws Exception {
        List<ParamEntity> fields = reflectParameter.reflect(Dummy.class);

        // modify uuids to have a empty value at index 0
        Map<String, List<String>> params = FixtureFactory.makeForDummyNestedParameters();
        params.remove("opt_uuid");

        Dummy actual = subject.to(Dummy.class, fields, params);

        assertNotNull(actual);
        assertNotNull(actual.getOptId());
        assertFalse(actual.getOptId().isPresent());
    }

    @Test
    public void toWhenMissingReqFieldShouldThrowRequiredException() throws Exception {
        List<ParamEntity> fields = reflectParameter.reflect(Dummy.class);

        // remove the key, string
        Map<String, List<String>> params = FixtureFactory.makeNotNestedParameters();
        params.remove("string");

        RequiredException actualException = null;
        try {
            subject.to(Dummy.class, fields, params);
        } catch (RequiredException e) {
            actualException = e;
        }

        assertNotNull(actualException);
        assertEquals("string", actualException.getField());
        assertEquals("string", actualException.getParam());
        assertTrue(actualException.getCause() instanceof ParamIsNullError);
        Dummy actual = (Dummy) actualException.getTarget();

        assertNotNull(actual);
    }

    @Test
    public void toWhenReqFieldHasInvalidValueThrowRequiredException() throws Exception {
        List<ParamEntity> fields = reflectParameter.reflect(Dummy.class);

        // remove the key, string
        Map<String, List<String>> params = FixtureFactory.makeNotNestedParameters();
        params.put("string", Arrays.asList("string2"));

        RequiredException actualException = null;
        try {
            subject.to(Dummy.class, fields, params);
        } catch (RequiredException e) {
            actualException = e;
        }

        assertNotNull(actualException);
        assertEquals("string", actualException.getField());
        assertEquals("string", actualException.getParam());
        assertTrue(actualException.getCause() instanceof ValueException);

        ValueException actualVE =  (ValueException) actualException.getCause();
        assertEquals("string", actualVE.getParam());
        assertEquals("string", actualVE.getField());
        assertEquals("string2", actualVE.getValue());

        Dummy actual = (Dummy) actualException.getTarget();

        assertNotNull(actual);
    }

    @Test
    public void toWhenOptFieldIsEmptyShouldThrowOptionalException() throws Exception {
        List<ParamEntity> fields = reflectParameter.reflect(Dummy.class);

        // modify uuids to have a empty value at index 0
        Map<String, List<String>> params = FixtureFactory.makeNotNestedParameters();
        List<String> uuid = Arrays.asList("");
        params.put("opt_uuid", uuid);

        OptionalException actualException = null;
        try {
            subject.to(Dummy.class, fields, params);
        } catch (OptionalException e) {
            actualException = e;
        }

        assertNotNull(actualException);
        Dummy actual = (Dummy) actualException.getTarget();

        assertNotNull(actual);

        // should have populated all the vars above the failure point

        String expectedString = params.get("string").get(0);
        assertNotNull(actual.getString());
        assertEquals(expectedString, actual.getString());

        UUID expectedId = UUID.fromString(params.get("uuid").get(0));
        assertNotNull(actual.getId());
        assertEquals(expectedId, actual.getId());

        URI expectedUri = new URI(params.get("uri").get(0));
        assertNotNull(actual.getUri());
        assertEquals(expectedUri, actual.getUri());

        // lists
        assertNotNull(actual.getStrings());
        assertEquals(3, actual.getStrings().size());
        assertEquals("string1", actual.getStrings().get(0));
        assertEquals("string2", actual.getStrings().get(1));
        assertEquals("string3", actual.getStrings().get(2));

        assertNotNull(actual.getIds());
        assertEquals(1, actual.getIds().size());
        assertEquals(expectedId, actual.getIds().get(0));

        assertNotNull(actual.getUris());
        assertEquals(1, actual.getUris().size());
        assertEquals(expectedUri, actual.getUris().get(0));

        // opts
        assertNotNull(actual.getOptString());
        assertTrue(actual.getOptString().isPresent());
        assertEquals(expectedString, actual.getOptString().get());

        // these should not have been assigned.
        assertNull(actual.getOptId());
        assertNull(actual.getOptUri());

        // not annotated field - should not have been assigned.
        assertNull(actual.getNotAnnotated());
    }

    @Test
    public void toWhenReqFieldInvalidValueShouldThrowRequiredException() throws Exception {
        List<ParamEntity> fields = reflectParameter.reflect(Dummy.class);
        Map<String, List<String>> params = FixtureFactory.makeNotNestedParameters();
        params.put("uuid", Arrays.asList("invalid-uuid"));

        RequiredException actualException = null;
        try {
            subject.to(Dummy.class, fields, params);
        } catch (RequiredException e) {
            actualException = e;
        }

        assertNotNull(actualException);
        assertTrue(actualException.getCause() instanceof ConstructException);
        assertEquals("id", actualException.getField());
        assertEquals("uuid", actualException.getParam());

        // inspect the ConstructException
        ConstructException actualCause = (ConstructException) actualException.getCause();
        assertNotNull(actualCause);
        assertTrue(actualCause.getCause() instanceof IllegalArgumentException);

        Dummy actual = (Dummy) actualException.getTarget();

        assertNotNull(actual);
    }

    @Test
    public void toWhenOptFieldInvalidValueShouldThrowOptionalException() throws Exception {
        List<ParamEntity> fields = reflectParameter.reflect(Dummy.class);
        Map<String, List<String>> params = FixtureFactory.makeNotNestedParameters();
        params.put("opt_uuid", Arrays.asList("invalid-uuid"));

        OptionalException actualException = null;
        try {
            subject.to(Dummy.class, fields, params);
        } catch (OptionalException e) {
            actualException = e;
        }

        assertNotNull(actualException);
        assertTrue(actualException.getCause() instanceof ConstructException);
        assertEquals("optId", actualException.getField());
        assertEquals("opt_uuid", actualException.getParam() );

        // inspect the ConstructException
        ConstructException actualCause = (ConstructException) actualException.getCause();
        assertNotNull(actualCause);
        assertTrue(actualCause.getCause() instanceof IllegalArgumentException);

        Dummy actual = (Dummy) actualException.getTarget();

        assertNotNull(actual);
    }

    @Test
    public void toWhenMissingReqNestedFieldShouldThrowRequiredException() throws Exception {
        List<ParamEntity> fields = reflectParameter.reflect(Dummy.class);

        Map<String, List<String>> params = FixtureFactory.makeNotNestedParameters();

        RequiredException actualException = null;
        try {
            subject.to(Dummy.class, fields, params);
        } catch (RequiredException e) {
            actualException = e;
        }

        assertNotNull(actualException);
        assertEquals("nested", actualException.getField());
        assertNull(actualException.getParam());
        assertTrue(actualException.getCause() instanceof ParamIsNullError);
        Dummy actual = (Dummy) actualException.getTarget();

        assertNotNull(actual);
    }
}