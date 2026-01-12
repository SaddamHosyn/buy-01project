package ax.gritlab.buy_01.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Product request DTO.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public final class ProductRequest {

    /**
     * Minimum length for product name.
     */
    private static final int MIN_NAME_LENGTH = 2;

    /**
     * Maximum length for product name.
     */
    private static final int MAX_NAME_LENGTH = 100;

    /**
     * Maximum length for description.
     */
    private static final int MAX_DESCRIPTION_LENGTH = 500;

    /**
     * Product name.
     */
    @NotNull(message = "Product name is required")
    @Size(min = MIN_NAME_LENGTH, max = MAX_NAME_LENGTH,
            message = "Product name must be between 2 and 100 characters")
    private String name;

    /**
     * Product description.
     */
    @Size(max = MAX_DESCRIPTION_LENGTH,
            message = "Description must not exceed 500 characters")
    private String description;

    /**
     * Product price.
     */
    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be greater than or equal to 0")
    private Double price;

    /**
     * Product quantity.
     */
    @NotNull(message = "Quantity is required")
    @Min(value = 0,
            message = "Quantity must be greater than or equal to 0")
    private Integer quantity;
}
