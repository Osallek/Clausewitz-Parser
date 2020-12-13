package fr.osallek.clausewitzparser.common;

public class ClausewitzParseException extends RuntimeException {

    public ClausewitzParseException() {
    }

    public ClausewitzParseException(String message) {
        super(message);
    }

    public ClausewitzParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClausewitzParseException(Throwable cause) {
        super(cause);
    }

    public ClausewitzParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
