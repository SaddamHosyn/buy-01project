package ax.gritlab.buy_01.media.exception;

/**
 * Exception thrown when a user is not authorized to perform an action.
 */
public class UnauthorizedException extends RuntimeException {
    /**
     * Constructs a new UnauthorizedException with the specified message.
     *
     * @param message the detail message
     */
    public UnauthorizedException(final String message) {
        super(message);
    }
}
