package ax.gritlab.buy_01.product.service;

import ax.gritlab.buy_01.product.dto.ProductRequest;
import ax.gritlab.buy_01.product.dto.ProductResponse;
import ax.gritlab.buy_01.product.exception.ResourceNotFoundException;
import ax.gritlab.buy_01.product.exception.UnauthorizedException;
import ax.gritlab.buy_01.product.model.Product;
import ax.gritlab.buy_01.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Service for managing products.
 */
@Service
@RequiredArgsConstructor
public final class ProductService {

    /**
     * HTTP status code for not found.
     */
    private static final int HTTP_NOT_FOUND = 404;

    /**
     * HTTP status code for forbidden.
     */
    private static final int HTTP_FORBIDDEN = 403;

    /**
     * Product repository.
     */
    private final ProductRepository productRepository;

    /**
     * REST template for service calls.
     */
    private final RestTemplate restTemplate;

    /**
     * Kafka template for event publishing.
     */
    private final org.springframework.kafka.core.KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Object mapper for JSON operations.
     */
    private final ObjectMapper objectMapper;

    /**
     * Media service URL.
     */
    @Value("${media.service.url:http://media-service:8083/media}")
    private String mediaServiceUrl;

    /**
     * Media public URL.
     */
    @Value("${media.public.url:https://localhost:8443/api/media}")
    private String mediaPublicUrl;

    /**
     * Get all products.
     *
     * @return list of all products
     */
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::toProductResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get product by ID.
     *
     * @param id the product ID
     * @return the product response
     */
    public ProductResponse getProductById(final String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));
        return toProductResponse(product);
    }

    /**
     * Create a new product.
     *
     * @param request the product request
     * @param userId  the user ID
     * @return the created product response
     */
    public ProductResponse createProduct(
            final ProductRequest request,
            final String userId) {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .userId(userId)
                .createdAt(now.toLocalDateTime())
                .updatedAt(now.toLocalDateTime())
                .build();
        Product saved = productRepository.save(product);
        return toProductResponse(saved);
    }

    /**
     * Update an existing product.
     *
     * @param id      the product ID
     * @param request the product request
     * @param userId  the user ID
     * @return the updated product response
     */
    public ProductResponse updateProduct(
            final String id,
            final ProductRequest request,
            final String userId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));
        if (!product.getUserId().equals(userId)) {
            throw new UnauthorizedException(
                    "You do not have permission to update this product");
        }
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setUpdatedAt(
                ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        Product saved = productRepository.save(product);
        return toProductResponse(saved);
    }

    /**
     * Delete a product.
     *
     * @param id     the product ID
     * @param userId the user ID
     */
    public void deleteProduct(
            final String id,
            final String userId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));
        if (!product.getUserId().equals(userId)) {
            throw new UnauthorizedException(
                    "You do not have permission to delete this product");
        }
        List<String> mediaIds = product.getMediaIds();
        productRepository.delete(product);
        // Publish Kafka event for product deletion
        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("id", id);
            ArrayNode arr = node.putArray("mediaIds");
            if (mediaIds != null) {
                for (String m : mediaIds) {
                    arr.add(m);
                }
            }
            kafkaTemplate.send("product.deleted",
                    objectMapper.writeValueAsString(node));
        } catch (Exception e) {
            kafkaTemplate.send("product.deleted", id);
        }
    }

    /**
     * Associate media with a product.
     *
     * @param productId the product ID
     * @param mediaId   the media ID
     * @param userId    the user ID
     * @return the updated product response
     */
    public ProductResponse associateMedia(
            final String productId,
            final String mediaId,
            final String userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId));
        if (!product.getUserId().equals(userId)) {
            throw new UnauthorizedException(
                    "You do not have permission to modify this product");
        }
        product.getMediaIds().add(mediaId);
        Product saved = productRepository.save(product);

        // Call Media Service to update the productId in the media record
        try {
            String url = mediaServiceUrl + "/images/" + mediaId
                    + "/product/" + productId + "?userId=" + userId;
            restTemplate.put(url, null);
        } catch (Exception e) {
            // Log the error but don't fail the product update
            System.err.println(
                    "Failed to update media productId: "
                            + e.getMessage());
        }

        return toProductResponse(saved);
    }

    /**
     * Remove media ID from product's mediaIds array.
     * Called by Media Service when media is deleted.
     *
     * @param productId the product ID
     * @param mediaId   the media ID to remove
     */
    public void removeMediaFromProduct(
            final String productId,
            final String mediaId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException(
                        "Product not found"));

        product.getMediaIds().remove(mediaId);
        productRepository.save(product);
    }

    /**
     * Clean up all orphaned media IDs from products.
     * This removes media IDs that no longer exist in the media database.
     *
     * @return cleanup result message
     */
    public String cleanupOrphanedMedia() {
        List<Product> products = productRepository.findAll();
        int totalCleaned = 0;

        for (Product product : products) {
            List<String> validMediaIds = new ArrayList<>();

            // Check each media ID to see if it still exists
            for (String mediaId : product.getMediaIds()) {
                try {
                    // Try to call media service to check if media exists
                    String url = mediaServiceUrl + "/images/" + mediaId;
                    restTemplate.headForHeaders(url);
                    // If no exception, media exists
                    validMediaIds.add(mediaId);
                } catch (org.springframework.web.client.HttpClientErrorException e) {
                    // Media doesn't exist or forbidden - remove it
                    if (e.getStatusCode().value() == HTTP_NOT_FOUND
                            || e.getStatusCode().value() == HTTP_FORBIDDEN) {
                        System.out.println(
                                "Removing orphaned/inaccessible media ID: "
                                        + mediaId + " from product: "
                                        + product.getId());
                        totalCleaned++;
                    } else {
                        // Other error - keep the media ID
                        validMediaIds.add(mediaId);
                    }
                } catch (Exception e) {
                    // Unknown error - keep the media ID to be safe
                    validMediaIds.add(mediaId);
                }
            }

            // Update product if any media IDs were removed
            if (validMediaIds.size() != product.getMediaIds().size()) {
                int removedCount = product.getMediaIds().size()
                        - validMediaIds.size();
                product.setMediaIds(validMediaIds);
                productRepository.save(product);
                System.out.println(
                        "Cleaned product: " + product.getId()
                                + " - Removed " + removedCount
                                + " orphaned media IDs");
            }
        }

        return "Cleaned up " + totalCleaned
                + " orphaned media references from products";
    }

    /**
     * Convert Product entity to ProductResponse DTO with imageUrls.
     *
     * @param product the product entity
     * @return the product response DTO
     */
    private ProductResponse toProductResponse(final Product product) {
        // Convert mediaIds to image URLs using public URL
        List<String> imageUrls = product.getMediaIds().stream()
                .map(mediaId -> mediaPublicUrl + "/images/" + mediaId)
                .collect(Collectors.toList());

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getQuantity())
                .sellerId(product.getUserId())
                .mediaIds(product.getMediaIds())
                .imageUrls(imageUrls)
                .createdAt(product.getCreatedAt() != null
                        ? product.getCreatedAt()
                                .atZone(ZoneOffset.UTC).toString()
                        : null)
                .updatedAt(product.getUpdatedAt() != null
                        ? product.getUpdatedAt()
                                .atZone(ZoneOffset.UTC).toString()
                        : null)
                .build();
    }

    /**
     * Delete all products for a user and publish product.deleted events.
     *
     * @param userId the user ID
     */
    public void deleteProductsByUserId(final String userId) {
        List<Product> products = productRepository.findByUserId(userId);
        for (Product product : products) {
            List<String> mediaIds = product.getMediaIds();
            productRepository.delete(product);
            try {
                ObjectNode node = objectMapper.createObjectNode();
                node.put("id", product.getId());
                ArrayNode arr = node.putArray("mediaIds");
                if (mediaIds != null) {
                    for (String m : mediaIds) {
                        arr.add(m);
                    }
                }
                kafkaTemplate.send("product.deleted",
                        objectMapper.writeValueAsString(node));
            } catch (Exception e) {
                kafkaTemplate.send("product.deleted", product.getId());
            }
        }
    }
}
