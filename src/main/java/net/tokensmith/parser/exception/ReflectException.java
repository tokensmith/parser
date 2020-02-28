package net.tokensmith.parser.exception;

public class ReflectException extends Exception {
    private String className;

    public ReflectException(String message, Throwable cause, String className) {
        super(message, cause);
        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}
