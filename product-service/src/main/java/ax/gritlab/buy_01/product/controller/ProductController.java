package ax.gritlab.buy_01.product.controller;

import ax.gritlab.buy_01.product.dto.ProductRequest;
import ax.gritlab.buy_01.product.dto.ProductResponse;
import ax.gritlab.buy_01.product.model.User;
import ax.gritlab.buy_01.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for product operations.
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public final class ProductController {

    /**
     * Product service.
     */
    /**
     * Product service.
     */
    private final ProductService productService;

    /**
     * Get all products.
     *
     * @return list of all products
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    /**
     * Get product by ID.
     *
     * @param id the product ID
     * @return the product
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable final String id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * Create a new product.
     *
     * @param request        the product request
     * @param authentication the authentication
     * @return the created product
     */
    @PostMapping
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody final ProductRequest request,
            final Authentication authentication) {
        String userId = ((User) authentication.getPrincipal()).getId();
        ProductResponse createdProduct = productService.createProduct(request, userId);
        return ResponseEntity.ok(createdProduct);
    }

    /**
     * Update an existing product.
     *
     * @param id             the product ID
     * @param request        the product request
     * @param authentication the authentication
     * @return the updated product
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable final String id,
            @Valid @RequestBody final ProductRequest request,
            final Authentication authentication) {
        String userId = ((User) authentication.getPrincipal()).getId();
        ProductResponse updatedProduct = productService.updateProduct(id, request, userId);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * Delete a product.
     *
     * @param id             the product ID
     * @param authentication the authentication
     * @return no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable final String id,
            final Authentication authentication) {
        String userId = ((User) authentication.getPrincipal()).getId();
        productService.deleteProduct(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Associate media with a product.
     *
     * @param productId      the product ID
     * @param mediaId        the media ID
     * @param authentication the authentication
     * @return the updated product
     */
    @PostMapping("/{productId}/media/{mediaId}")
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<ProductResponse> associateMedia(
            @PathVariable final String productId,
            @PathVariable final String mediaId,
            final Authentication authentication) {
        String userId = ((User) authentication.getPrincipal()).getId();
        ProductResponse updatedProduct = productService.associateMedia(productId, mediaId, userId);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * Remove media ID from product's mediaIds array.
     * Called by Media Service when media is deleted.
     *
     * @param productId the ID of the product to update
     * @param mediaId   the ID of the media to remove
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/{productId}/remove-media/{mediaId}")
    public ResponseEntity<Void> removeMediaFromProduct(
            @PathVariable final String productId,
            @PathVariable final String mediaId) {
        productService.removeMediaFromProduct(productId, mediaId);
        return ResponseEntity.ok().build();
    }

    /**
     * Clean up all orphaned media IDs from products.
     * This removes media IDs that no longer exist in the media database.
     *
     * @return ResponseEntity with cleanup result message
     */
    @PostMapping("/cleanup-orphaned-media")
    public ResponseEntity<String> cleanupOrphanedMedia() {
        String result = productService.cleanupOrphanedMedia();
        return ResponseEntity.ok(result);
    }
}
