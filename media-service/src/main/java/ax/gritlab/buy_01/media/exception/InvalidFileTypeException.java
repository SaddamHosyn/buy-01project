package ax.gritlab.buy_01.media.exception;

/**
 * Exception thrown when an invalid file type is provided.
 */
public class InvalidFileTypeException extends RuntimeException {
    /**
     * Constructs a new InvalidFileTypeException with specified message.
     *
     * @param message the detail message
     */
    public InvalidFileTypeException(final String message) {
        super(message);
    }
}
