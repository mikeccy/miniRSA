package edu.cit595.qyccy.exception;

public class InvalidDataFormatException extends Exception {

    private static final long serialVersionUID = 6568450636402908041L;
    
    public InvalidDataFormatException(String message) {
        super(message);
    }
}
