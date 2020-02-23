package net.tokensmith.parser;

import net.tokensmith.parser.builder.exception.ConstructException;
import helper.Dummy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class ReflectParameterTest {
    private ReflectParameter subject;

    // names of fields expected
    private List<String> names = Arrays.asList(
            "string", "id", "uri",
            "strings", "ids", "uris",
            "optString", "optId", "optUri",
            "optList", "nested", "optNested"
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
        subject = new ReflectParameter(builders);
    }

    @Test
    public void reflectShouldFindAllFields() throws Exception {
        List<ParamEntity> actuals = subject.reflect(Dummy.class);
        assertNotNull(actuals);
        assertEquals(actuals.size(), names.size());

        for(ParamEntity actual: actuals) {
            boolean found = names.contains(actual.getField().getName());
            assertTrue(found, "could not find field: " + actual.getField().getName());
        }
    }


}