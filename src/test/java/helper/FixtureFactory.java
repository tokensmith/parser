package helper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FixtureFactory {

    public static Map<String, List<String>> makeNotNestedParameters() {
        Map<String, List<String>> parameters = new HashMap<>();

        List<String> strings = Arrays.asList("string1");
        parameters.put("string", strings);

        List<String> strings2 = Arrays.asList("string1 string2 string3");
        parameters.put("strings", strings2);

        parameters.put("opt_string", strings);

        List<String> uuids = Arrays.asList(UUID.randomUUID().toString());
        parameters.put("uuid", uuids);
        parameters.put("uuids", uuids);
        parameters.put("opt_uuid", uuids);

        List<String> uris = Arrays.asList("https://tokensmith.net");
        parameters.put("uri", uris);
        parameters.put("uris", uris);
        parameters.put("opt_uri", uris);

        parameters.put("opt_list",Arrays.asList("opt_list1"));

        return parameters;
    }

    public static Map<String, List<String>> makeForDummyNestedParameters() {
        Map<String, List<String>> parameters = new HashMap<>();

        List<String> strings = Arrays.asList("string1");
        parameters.put("string", strings);
        parameters.put("nested.string", strings);

        List<String> strings2 = Arrays.asList("string1 string2 string3");
        parameters.put("strings", strings2);
        parameters.put("nested.strings", strings2);

        parameters.put("opt_string", strings);
        parameters.put("nested.opt_string", strings);

        List<String> uuids = Arrays.asList(UUID.randomUUID().toString());
        parameters.put("uuid", uuids);
        parameters.put("nested.uuid", uuids);
        parameters.put("uuids", uuids);
        parameters.put("nested.uuids", uuids);
        parameters.put("opt_uuid", uuids);
        parameters.put("nested.opt_uuid", uuids);

        List<String> uris = Arrays.asList("https://tokensmith.net");
        parameters.put("uri", uris);
        parameters.put("nested.uri", uris);
        parameters.put("uris", uris);
        parameters.put("nested.uris", uris);
        parameters.put("opt_uri", uris);
        parameters.put("nested.opt_uri", uris);

        parameters.put("opt_list",Arrays.asList("opt_list1"));
        parameters.put("nested.opt_list", uris);

        return parameters;
    }

    public static Map<String, List<String>> makeAllNestedParameters() {
        Map<String, List<String>> parameters = new HashMap<>();

        List<String> strings = Arrays.asList("string1");
        parameters.put("nested.string", strings);

        List<String> strings2 = Arrays.asList("string1 string2 string3");
        parameters.put("nested.strings", strings2);

        parameters.put("nested.opt_string", strings);

        List<String> uuids = Arrays.asList(UUID.randomUUID().toString());
        parameters.put("nested.uuid", uuids);
        parameters.put("nested.uuids", uuids);
        parameters.put("nested.opt_uuid", uuids);

        List<String> uris = Arrays.asList("https://tokensmith.net");
        parameters.put("nested.uri", uris);
        parameters.put("nested.uris", uris);
        parameters.put("nested.opt_uri", uris);

        parameters.put("nested.opt_list",Arrays.asList("opt_list1"));

        return parameters;
    }
}
