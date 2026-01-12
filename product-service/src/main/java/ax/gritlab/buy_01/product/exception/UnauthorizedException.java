package ax.gritlab.buy_01.product.exception;

/**
 * Exception thrown when user is not authorized.
 */
public final class UnauthorizedException extends RuntimeException {
    /**
     * Constructs a new UnauthorizedException.
     *
     * @param message the error message
     */
    public UnauthorizedException(final String message) {
        super(message);
    }
}

