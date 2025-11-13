package ax.gritlab.buy_01.product.service;

import ax.gritlab.buy_01.product.dto.ProductRequest;
import ax.gritlab.buy_01.product.dto.ProductResponse;
import ax.gritlab.buy_01.product.model.Product;
import ax.gritlab.buy_01.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final RestTemplate restTemplate;

    @Value("${media.service.url:http://localhost:8080/api/media}")
    private String mediaServiceUrl;

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::toProductResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return toProductResponse(product);
    }

    public ProductResponse createProduct(ProductRequest request, String userId) {
        LocalDateTime now = LocalDateTime.now();
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .userId(userId)
                .createdAt(now)
                .updatedAt(now)
                .build();
        Product saved = productRepository.save(product);
        return toProductResponse(saved);
    }

    public ProductResponse updateProduct(String id, ProductRequest request, String userId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        if (!product.getUserId().equals(userId)) {
            throw new RuntimeException("User does not have permission to update this product");
        }
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setUpdatedAt(LocalDateTime.now());
        Product saved = productRepository.save(product);
        return toProductResponse(saved);
    }

    public void deleteProduct(String id, String userId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        if (!product.getUserId().equals(userId)) {
            throw new RuntimeException("User does not have permission to delete this product");
        }
        productRepository.delete(product);
    }

    public ProductResponse associateMedia(String productId, String mediaId, String userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        if (!product.getUserId().equals(userId)) {
            throw new RuntimeException("User does not have permission to modify this product");
        }
        product.getMediaIds().add(mediaId);
        Product saved = productRepository.save(product);
        
        // Call Media Service to update the productId in the media record
        try {
            String url = mediaServiceUrl + "/images/" + mediaId + "/product/" + productId + "?userId=" + userId;
            restTemplate.put(url, null);
        } catch (Exception e) {
            // Log the error but don't fail the product update
            System.err.println("Failed to update media productId: " + e.getMessage());
        }
        
        return toProductResponse(saved);
    }

    /**
     * Convert Product entity to ProductResponse DTO with imageUrls
     */
    private ProductResponse toProductResponse(Product product) {
        // Convert mediaIds to image URLs
        List<String> imageUrls = product.getMediaIds().stream()
                .map(mediaId -> mediaServiceUrl + "/images/" + mediaId)
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
                .createdAt(product.getCreatedAt() != null ? product.getCreatedAt().toString() : null)
                .updatedAt(product.getUpdatedAt() != null ? product.getUpdatedAt().toString() : null)
                .build();
    }
}
