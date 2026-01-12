package ax.gritlab.buy_01.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Product response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class ProductResponse {
    /**
     * Product ID.
     */
    private String id;

    /**
     * Product name.
     */
    private String name;

    /**
     * Product description.
     */
    private String description;

    /**
     * Product price.
     */
    private Double price;

    /**
     * Product stock quantity.
     */
    private Integer stock;

    /**
     * Seller ID.
     */
    private String sellerId;

    /**
     * List of media IDs.
     */
    private List<String> mediaIds;

    /**
     * List of image URLs.
     */
    private List<String> imageUrls;

    /**
     * Creation timestamp.
     */
    private String createdAt;

    /**
     * Last update timestamp.
     */
    private String updatedAt;
}
