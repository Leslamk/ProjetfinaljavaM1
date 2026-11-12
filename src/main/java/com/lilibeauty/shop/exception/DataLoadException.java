package com.lilibeauty.shop.exception;

/**
 * Thrown when the data file is missing, unreadable or contains an invalid line.
 * Wraps lower-level causes (IOException, NumberFormatException) so the rest of the
 * application only deals with one meaningful exception type.
 */
public class DataLoadException extends Exception {

    public DataLoadException(String message) {
        super(message);
    }

    public DataLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
