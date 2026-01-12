package ax.gritlab.buy_01.product.exception;

/**
 * Exception thrown when a resource is not found.
 */
public final class ResourceNotFoundException extends RuntimeException {
    /**
     * Constructs a new ResourceNotFoundException.
     *
     * @param message the error message
     */
    public ResourceNotFoundException(final String message) {
        super(message);
    }
}
