package ax.gritlab.buy_01.product.controller;

import ax.gritlab.buy_01.product.dto.ProductRequest;
import ax.gritlab.buy_01.product.model.Product;
import ax.gritlab.buy_01.product.model.User;
import ax.gritlab.buy_01.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<Product> createProduct(@RequestBody ProductRequest request, Authentication authentication) {
        String userId = ((User) authentication.getPrincipal()).getId();
        Product createdProduct = productService.createProduct(request, userId);
        return ResponseEntity.ok(createdProduct);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<Product> updateProduct(@PathVariable String id, @RequestBody ProductRequest request, Authentication authentication) {
        String userId = ((User) authentication.getPrincipal()).getId();
        Product updatedProduct = productService.updateProduct(id, request, userId);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id, Authentication authentication) {
        String userId = ((User) authentication.getPrincipal()).getId();
        productService.deleteProduct(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{productId}/media/{mediaId}")
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<Product> associateMedia(@PathVariable String productId, @PathVariable String mediaId, Authentication authentication) {
        String userId = ((User) authentication.getPrincipal()).getId();
        Product updatedProduct = productService.associateMedia(productId, mediaId, userId);
        return ResponseEntity.ok(updatedProduct);
    }
}
