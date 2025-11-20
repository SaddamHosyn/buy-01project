package ax.gritlab.buy_01.product.service;

import ax.gritlab.buy_01.product.dto.ProductRequest;
import ax.gritlab.buy_01.product.dto.ProductResponse;
import ax.gritlab.buy_01.product.model.Product;
import ax.gritlab.buy_01.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    // Delete all products for a user and publish product.deleted events
    public void deleteProductsByUserId(String userId) {
        List<Product> products = productRepository.findByUserId(userId);
        for (Product product : products) {
            productRepository.delete(product);
            kafkaTemplate.send("product.deleted", product.getId());
        }
    }

    private final ProductRepository productRepository;
    private final RestTemplate restTemplate;
    private final org.springframework.kafka.core.KafkaTemplate<String, String> kafkaTemplate;

    @Value("${media.service.url:http://media-service:8083/media}")
    private String mediaServiceUrl;

    @Value("${media.public.url:https://localhost:8443/api/media}")
    private String mediaPublicUrl;

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
        product.setUpdatedAt(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
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
        // Publish Kafka event for product deletion
        kafkaTemplate.send("product.deleted", id);
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
        // Convert mediaIds to image URLs using public URL for browser access
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
                .createdAt(product.getCreatedAt() != null ? product.getCreatedAt().atZone(ZoneOffset.UTC).toString()
                        : null)
                .updatedAt(product.getUpdatedAt() != null ? product.getUpdatedAt().atZone(ZoneOffset.UTC).toString()
                        : null)
                .build();
    }
}
