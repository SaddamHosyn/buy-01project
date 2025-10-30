package ax.gritlab.buy_01.product.service;

import ax.gritlab.buy_01.product.dto.ProductRequest;
import ax.gritlab.buy_01.product.model.Product;
import ax.gritlab.buy_01.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(String id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public Product createProduct(ProductRequest request, String userId) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .userId(userId)
                .build();
        return productRepository.save(product);
    }

    public Product updateProduct(String id, ProductRequest request, String userId) {
        Product product = getProductById(id);
        if (!product.getUserId().equals(userId)) {
            throw new RuntimeException("User does not have permission to update this product");
        }
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        return productRepository.save(product);
    }

    public void deleteProduct(String id, String userId) {
        Product product = getProductById(id);
        if (!product.getUserId().equals(userId)) {
            throw new RuntimeException("User does not have permission to delete this product");
        }
        productRepository.delete(product);
    }

    public Product associateMedia(String productId, String mediaId, String userId) {
        Product product = getProductById(productId);
        if (!product.getUserId().equals(userId)) {
            throw new RuntimeException("User does not have permission to modify this product");
        }
        // In a real microservice, we might call the media-service to validate the mediaId
        // For now, we trust the ID provided by the authenticated user.
        product.getMediaIds().add(mediaId);
        return productRepository.save(product);
    }
}
