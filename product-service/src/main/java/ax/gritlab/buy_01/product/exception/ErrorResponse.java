package ax.gritlab.buy_01.product.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Error response DTO.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public final class ErrorResponse {
    /**
     * Timestamp of the error.
     */
    private LocalDateTime timestamp;

    /**
     * HTTP status code.
     */
    private int status;

    /**
     * Error type.
     */
    private String error;

    /**
     * Error message.
     */
    private String message;
}

