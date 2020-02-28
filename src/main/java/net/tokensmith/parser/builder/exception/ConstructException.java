package net.tokensmith.parser.builder.exception;

public class ConstructException extends RuntimeException {
    private String value;

    public ConstructException(String message, Throwable cause, String value) {
        super(message, cause);
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
