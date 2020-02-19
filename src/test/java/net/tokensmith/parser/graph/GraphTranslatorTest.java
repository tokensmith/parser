package net.tokensmith.parser.graph;

import net.tokensmith.parser.helper.FixtureFactory;
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

        List<GraphNode<NodeData>> actual = subject().to(from);

        assertNotNull(actual);
        assertEquals(10, actual.size());
    }

    @Test
    public void toWhenNestedShouldTranslate() {
        Map<String, List<String>> from = FixtureFactory.makeNestedParameters();

        List<GraphNode<NodeData>> actual = subject().to(from);

        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertEquals(10, actual.get(0).getChildren().size());
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
}