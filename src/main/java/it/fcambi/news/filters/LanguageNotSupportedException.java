package it.fcambi.news.filters;

/**
 * Created by Francesco on 18/05/16.
 */
public class LanguageNotSupportedException extends RuntimeException {

    public LanguageNotSupportedException() {
    }

    public LanguageNotSupportedException(String message) {
        super(message);
    }

    public LanguageNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public LanguageNotSupportedException(Throwable cause) {
        super(cause);
    }

    public LanguageNotSupportedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
