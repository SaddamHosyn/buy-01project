package ax.gritlab.buy_01.user.exception;

/**
 * Exception thrown when a user is unauthorized to access a resource.
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
