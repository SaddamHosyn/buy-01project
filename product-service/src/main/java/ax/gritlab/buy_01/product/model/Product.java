package ax.gritlab.buy_01.product.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Product entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public final class Product {

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
     * Product ID.
     */
    @Id
    private String id;

    /**
     * Product name.
     */
    @NotNull
    @Size(min = MIN_NAME_LENGTH, max = MAX_NAME_LENGTH)
    private String name;

    /**
     * Product description.
     */
    @Size(max = MAX_DESCRIPTION_LENGTH)
    private String description;

    /**
     * Product price.
     */
    @NotNull
    private Double price;

    /**
     * Product quantity.
     */
    @NotNull
    private Integer quantity;

    /**
     * User ID of the seller.
     */
    @NotNull
    private String userId;

    /**
     * List of media IDs associated with the product.
     */
    @Builder.Default
    private List<String> mediaIds = new ArrayList<>();

    /**
     * Creation timestamp.
     */
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * Last update timestamp.
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
