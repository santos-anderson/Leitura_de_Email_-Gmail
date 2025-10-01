package com.gmailreader.exception;


public class GmailReaderException extends RuntimeException {

    public GmailReaderException(String message) {
        super(message);
    }

    public GmailReaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public GmailReaderException(Throwable cause) {
        super(cause);
    }
}
