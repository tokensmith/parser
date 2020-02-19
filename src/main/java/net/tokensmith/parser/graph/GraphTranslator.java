package net.tokensmith.parser.graph;

import net.tokensmith.parser.model.NodeData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class GraphTranslator {
    private static Pattern NESTED_PATTERN = Pattern.compile("\\.");

    public List<GraphNode<NodeData>> to(Map<String, List<String>> from) {
        List<GraphNode<NodeData>> to = new ArrayList<>();

        // used only to have faster look ups for insert as child.
        Map<String, GraphNode<NodeData>> rootNodes = new HashMap<>();

        for(Map.Entry<String, List<String>> entry: from.entrySet()) {
            List<String> parsedKeys = matches(entry.getKey());

            if (parsedKeys.size() > 0) {
                // add with traversal
                GraphNode<NodeData> rootNode = rootNodes.get(parsedKeys.get(0));
                String dataKey = parsedKeys.get(parsedKeys.size()-1);
                NodeData item = new NodeData(dataKey, entry.getValue());

                if (rootNode == null ) {
                    // add with traversal with no root.
                    rootNode = new GraphNode.Builder<NodeData>()
                        .id(parsedKeys.get(0))
                        .build();
                    rootNodes.put(parsedKeys.get(0), rootNode);
                    to.add(rootNode);
                }
                rootNode.insert(parsedKeys.listIterator(1), item);
            } else {
                // this as root.
                NodeData item = new NodeData(entry.getKey(), entry.getValue());
                GraphNode<NodeData> node = new GraphNode.Builder<NodeData>()
                        .id(entry.getKey())
                        .data(item)
                        .build();

                to.add(node);
                rootNodes.put(entry.getKey(), node);
            }
        }
        return to;
    }

    protected List<String> matches(String input) {
        return NESTED_PATTERN.splitAsStream(input).collect(Collectors.toList());
    }
}
