package skype.protocol;

public class BadPacketException extends RuntimeException {
    public BadPacketException(String message) {
        super(message);
    }

    public BadPacketException(String message, Throwable cause) {
        super(message, cause);
    }
}