package net.tokensmith.parser.graph;

import helper.FixtureFactory;
import net.tokensmith.parser.model.NodeData;
import org.junit.jupiter.api.Test;


import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GraphTranslatorTest {

    public GraphTranslator subject() {
        return new GraphTranslator();
    }

    @Test
    public void toWhenNotNestedShouldTranslate() {
        Map<String, List<String>> from = FixtureFactory.makeNotNestedParameters();

        Map<String, GraphNode<NodeData>> actual = subject().to(from);

        assertNotNull(actual);
        assertEquals(10, actual.size());

        assertGraphNode(actual.get("string"), "string", 1, from.get("string"));
        assertGraphNode(actual.get("strings"), "strings", 1, from.get("strings"));
        assertGraphNode(actual.get("opt_string"), "opt_string", 1, from.get("opt_string"));
        assertGraphNode(actual.get("uuid"), "uuid", 1, from.get("uuid"));
        assertGraphNode(actual.get("uuids"), "uuids", 1, from.get("uuids"));
        assertGraphNode(actual.get("opt_uuid"), "opt_uuid", 1, from.get("opt_uuid"));
        assertGraphNode(actual.get("uri"), "uri", 1, from.get("uri"));
        assertGraphNode(actual.get("uris"), "uris", 1, from.get("uris"));
        assertGraphNode(actual.get("opt_uri"), "opt_uri", 1, from.get("opt_uri"));
        assertGraphNode(actual.get("opt_list"), "opt_list", 1, from.get("opt_list"));
    }

    @Test
    public void toWhenNestedShouldTranslate() {
        Map<String, List<String>> from = FixtureFactory.makeAllNestedParameters();

        Map<String, GraphNode<NodeData>> actual = subject().to(from);

        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertEquals(10, actual.get("nested").getChildren().size());

        GraphNode<NodeData> actualRoot = actual.get("nested");
        // assert proper nodes are created.
        assertEquals("nested", actualRoot.getId());
        assertTrue(actualRoot.getChildren().containsKey("string"));
        assertTrue(actualRoot.getChildren().containsKey("strings"));
        assertTrue(actualRoot.getChildren().containsKey("opt_string"));
        assertTrue(actualRoot.getChildren().containsKey("uuid"));
        assertTrue(actualRoot.getChildren().containsKey("string"));
        assertTrue(actualRoot.getChildren().containsKey("uuids"));
        assertTrue(actualRoot.getChildren().containsKey("opt_uuid"));
        assertTrue(actualRoot.getChildren().containsKey("uri"));
        assertTrue(actualRoot.getChildren().containsKey("uris"));
        assertTrue(actualRoot.getChildren().containsKey("opt_uri"));

        // assert data nodes are there or not there.
        assertNull(actualRoot.getData());

        GraphNode<NodeData> string = toNode(actualRoot, "string");
        assertGraphNode(string, "string", 1, from.get("nested.string"));

        GraphNode<NodeData> strings = toNode(actualRoot, "strings");
        assertGraphNode(strings, "strings", 1, from.get("nested.strings"));

        GraphNode<NodeData> optString = toNode(actualRoot, "opt_string");
        assertGraphNode(optString, "opt_string", 1, from.get("nested.opt_string"));

        GraphNode<NodeData> uuid = toNode(actualRoot, "uuid");
        assertGraphNode(uuid, "uuid", 1, from.get("nested.uuid"));;

        GraphNode<NodeData> uuids = toNode(actualRoot, "uuids");
        assertGraphNode(uuids, "uuids", 1, from.get("nested.uuids"));;

        GraphNode<NodeData> optUuid = toNode(actualRoot, "opt_uuid");
        assertGraphNode(optUuid, "opt_uuid", 1, from.get("nested.opt_uuid"));

        GraphNode<NodeData> uri = toNode(actualRoot, "uri");
        assertGraphNode(uri, "uri", 1, from.get("nested.uri"));

        GraphNode<NodeData> uris = toNode(actualRoot, "uris");
        assertGraphNode(uris, "uris", 1, from.get("nested.uris"));

        GraphNode<NodeData> optUri = toNode(actualRoot, "opt_uri");
        assertGraphNode(optUri, "opt_uri", 1, from.get("nested.opt_uri"));
    }

    @Test
    public void matchesWithOneNestedShouldBeOk() {
        List<String> actual = subject().matches("nested.id");

        assertNotNull(actual);
        assertEquals(2, actual.size());
        assertEquals("nested", actual.get(0));
        assertEquals("id", actual.get(1));
    }

    @Test
    public void matchesWithTwoNestedShouldBeOk() {
        List<String> actual = subject().matches("nested-one.nested-two.id");

        assertNotNull(actual);
        assertEquals(3, actual.size());
        assertEquals("nested-one", actual.get(0));
        assertEquals("nested-two", actual.get(1));
        assertEquals("id", actual.get(2));
    }

    public void assertGraphNode(GraphNode<NodeData> actual, String key, int length, List<String> values) {
        assertNotNull(actual.getData());
        assertEquals(key, actual.getData().getKey());
        assertEquals(length, actual.getData().getValues().size());
        for(String value: values) {
            assertTrue(actual.getData().getValues().contains(value));
        }
    }
    public GraphNode<NodeData> toNode(GraphNode<NodeData> node, String key) {
        return node.getChildren().get(key);
    }
}