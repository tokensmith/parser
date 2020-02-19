package net.tokensmith.parser.graph;


import java.util.LinkedHashMap;
import java.util.ListIterator;


public class GraphNode<T> {

    private String id;
    private T data;
    private GraphNode<T> parent;
    private LinkedHashMap<String, GraphNode<T>> children;

    public GraphNode(T data, String id, GraphNode<T> parent, LinkedHashMap<String, GraphNode<T>> children) {
        this.id = id;
        this.data = data;
        this.parent = parent;
        this.children = children;
    }

    public void addChild(String key, GraphNode<T> child) {
        this.children.put(key, child);
    }

    public GraphNode<T> getChild(String key) {
        return children.get(key);
    }

    public LinkedHashMap<String, GraphNode<T>> getChildren() {
        return children;
    }

    public GraphNode<T> insert(ListIterator<String> keys, T data) {
        if(keys.hasNext()) {
            String key = keys.next();
            GraphNode<T> child = getChild(key);
            if (child == null && keys.hasNext()) {
                // has more keys to iterate until termination.
                child = new GraphNode<T>(null, key, this, new LinkedHashMap<>());
                this.addChild(key, child);
            } else if (child == null) {
                // terminates here.
                child = new GraphNode<T>(data, key, this, new LinkedHashMap<>());
                this.addChild(key, child);
            }
            keys.nextIndex();
            return child.insert(keys, data);
        }
        return this;
    }

    @Override
    public String toString() {
        return "GraphNode{" +
                "id='" + id + '\'' +
                '}';
    }

    public static class Builder<T> {
        private T data;
        private String id;
        private GraphNode<T> parent;
        private LinkedHashMap<String, GraphNode<T>> children = new LinkedHashMap<>();


        public Builder<T> data(T data) {
            this.data = data;
            return this;
        }

        public Builder<T> id(String id) {
            this.id = id;
            return this;
        }

        public Builder<T> parent(GraphNode<T> parent) {
            this.parent = parent;
            return this;
        }

        public Builder<T> children(LinkedHashMap<String, GraphNode<T>> children) {
            this.children = children;
            return this;
        }

        public GraphNode<T> build() {
            return new GraphNode<T>(data, id, parent, children);
        }
    }
}
