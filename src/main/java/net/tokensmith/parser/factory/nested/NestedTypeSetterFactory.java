package net.tokensmith.parser.factory.nested;

import net.tokensmith.parser.ParamEntity;
import net.tokensmith.parser.graph.GraphNode;
import net.tokensmith.parser.model.NodeData;

public class NestedTypeSetterFactory {

    public NestedTypeSetter make(ParamEntity paramEntity, GraphNode<NodeData> node) {

        if (node != null) {
            if (paramEntity.isParameterized() && paramEntity.isOptional() && node.hasChildren()) {
                return new OptionalTypeSetter();
            } else if (paramEntity.isParameterized() && paramEntity.isOptional() && !node.hasChildren()) {
                return new OptionalTypeEmptySetter();
            } else {
                return new ReferenceTypeSetter();
            }
        } else {
            if (paramEntity.isParameterized() && paramEntity.isOptional()) {
                return new OptionalTypeEmptySetter();
            } else{
                return new ReferenceTypeSetter();
            }
        }
    }
}
