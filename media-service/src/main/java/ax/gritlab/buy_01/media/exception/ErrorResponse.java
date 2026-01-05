package ax.gritlab.buy_01.media.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents an error response returned by the API.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    /**
     * The timestamp when the error occurred.
     */
    private LocalDateTime timestamp;

    /**
     * The HTTP status code of the error.
     */
    private int status;

    /**
     * The error type or category.
     */
    private String error;

    /**
     * The detailed error message.
     */
    private String message;
}
