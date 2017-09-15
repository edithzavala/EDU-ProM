package org.eduprom.exceptions;

public class ExportFailedException extends MiningException {

    public ExportFailedException(String message){
        super(message);
    }

    public ExportFailedException(String message, Throwable ex){
        super(message, ex);
    }

    public ExportFailedException(Throwable ex){
        super(ex);
    }
}
