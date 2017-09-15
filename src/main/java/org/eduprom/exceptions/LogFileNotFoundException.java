package org.eduprom.exceptions;

public class LogFileNotFoundException extends MiningException {
    public LogFileNotFoundException(String message) {
        super(message);
    }

    public LogFileNotFoundException(String message, Throwable ex) {
        super(message, ex);
    }

    public LogFileNotFoundException(Throwable ex) {
        super(ex);
    }
}
