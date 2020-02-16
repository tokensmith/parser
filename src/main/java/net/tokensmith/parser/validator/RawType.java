package net.tokensmith.parser.validator;


public enum RawType {
    LIST ("java.util.List"),
    OPTIONAL ("java.util.Optional");

    private String typeName;

    RawType(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }
}
