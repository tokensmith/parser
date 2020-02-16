package net.tokensmith.parser.validator;


public enum SupportedTypes {
    UUID ("java.util.UUID");

    private String type;

    SupportedTypes(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
