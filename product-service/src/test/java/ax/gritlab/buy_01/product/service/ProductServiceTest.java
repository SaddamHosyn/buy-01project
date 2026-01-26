package ax.gritlab.buy_01.product.service;

import ax.gritlab.buy_01.product.dto.ProductRequest;
import ax.gritlab.buy_01.product.dto.ProductResponse;
import ax.gritlab.buy_01.product.exception.ResourceNotFoundException;
import ax.gritlab.buy_01.product.exception.UnauthorizedException;
import ax.gritlab.buy_01.product.model.Product;
import ax.gritlab.buy_01.product.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductService.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private ProductRequest productRequest;
    private final String userId = "user-123";
    private final String productId = "product-456";

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(productId)
                .name("Test Product")
                .description("Test Description")
                .price(99.99)
                .quantity(10)
                .userId(userId)
                .mediaIds(new ArrayList<>(Arrays.asList("media-1", "media-2")))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        productRequest = ProductRequest.builder()
                .name("Test Product")
                .description("Test Description")
                .price(99.99)
                .quantity(10)
                .build();

        ReflectionTestUtils.setField(productService, "mediaServiceUrl", "http://media-service:8083/media");
        ReflectionTestUtils.setField(productService, "mediaPublicUrl", "https://localhost:8443/api/media");
    }

    @Nested
    @DisplayName("getAllProducts Tests")
    class GetAllProductsTests {

        @Test
        @DisplayName("Should return all products")
        void getAllProducts_ReturnsAllProducts() {
            Product product2 = Product.builder()
                    .id("product-789")
                    .name("Product 2")
                    .description("Description 2")
                    .price(49.99)
                    .quantity(5)
                    .userId("user-456")
                    .mediaIds(new ArrayList<>())
                    .build();

            when(productRepository.findAll()).thenReturn(Arrays.asList(testProduct, product2));

            List<ProductResponse> responses = productService.getAllProducts();

            assertEquals(2, responses.size());
            verify(productRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no products")
        void getAllProducts_ReturnsEmptyList() {
            when(productRepository.findAll()).thenReturn(new ArrayList<>());

            List<ProductResponse> responses = productService.getAllProducts();

            assertTrue(responses.isEmpty());
        }

        @Test
        @DisplayName("Should include image URLs in response")
        void getAllProducts_IncludesImageUrls() {
            when(productRepository.findAll()).thenReturn(Arrays.asList(testProduct));

            List<ProductResponse> responses = productService.getAllProducts();

            assertEquals(2, responses.get(0).getImageUrls().size());
            assertTrue(responses.get(0).getImageUrls().get(0).contains("media-1"));
        }
    }

    @Nested
    @DisplayName("getProductById Tests")
    class GetProductByIdTests {

        @Test
        @DisplayName("Should return product when found")
        void getProductById_WhenFound_ReturnsProduct() {
            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

            ProductResponse response = productService.getProductById(productId);

            assertNotNull(response);
            assertEquals(productId, response.getId());
            assertEquals("Test Product", response.getName());
            assertEquals(99.99, response.getPrice());
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void getProductById_WhenNotFound_ThrowsException() {
            when(productRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> productService.getProductById("nonexistent"));
        }

        @Test
        @DisplayName("Should include seller ID in response")
        void getProductById_IncludesSellerId() {
            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

            ProductResponse response = productService.getProductById(productId);

            assertEquals(userId, response.getSellerId());
        }
    }

    @Nested
    @DisplayName("createProduct Tests")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product successfully")
        void createProduct_Success() {
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
                Product p = invocation.getArgument(0);
                p.setId("new-product-id");
                return p;
            });

            ProductResponse response = productService.createProduct(productRequest, userId);

            assertNotNull(response);
            assertEquals("new-product-id", response.getId());
            assertEquals("Test Product", response.getName());
            verify(productRepository, times(1)).save(any(Product.class));
        }

        @Test
        @DisplayName("Should set user ID on created product")
        void createProduct_SetsUserId() {
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
                Product p = invocation.getArgument(0);
                p.setId("new-id");
                return p;
            });

            ProductResponse response = productService.createProduct(productRequest, userId);

            assertEquals(userId, response.getSellerId());
        }

        @Test
        @DisplayName("Should set timestamps on created product")
        void createProduct_SetsTimestamps() {
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
                Product p = invocation.getArgument(0);
                p.setId("new-id");
                return p;
            });

            ProductResponse response = productService.createProduct(productRequest, userId);

            assertNotNull(response.getCreatedAt());
            assertNotNull(response.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("updateProduct Tests")
    class UpdateProductTests {

        @Test
        @DisplayName("Should update product successfully")
        void updateProduct_Success() {
            ProductRequest updateRequest = ProductRequest.builder()
                    .name("Updated Product")
                    .description("Updated Description")
                    .price(149.99)
                    .quantity(20)
                    .build();

            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

            ProductResponse response = productService.updateProduct(productId, updateRequest, userId);

            assertNotNull(response);
            assertEquals("Updated Product", response.getName());
            assertEquals(149.99, response.getPrice());
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void updateProduct_NotFound_ThrowsException() {
            when(productRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> productService.updateProduct("nonexistent", productRequest, userId));
        }

        @Test
        @DisplayName("Should throw exception when user is not owner")
        void updateProduct_NotOwner_ThrowsException() {
            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

            assertThrows(UnauthorizedException.class,
                    () -> productService.updateProduct(productId, productRequest, "other-user"));
        }

        @Test
        @DisplayName("Should update timestamp on update")
        void updateProduct_UpdatesTimestamp() {
            LocalDateTime originalUpdatedAt = testProduct.getUpdatedAt();
            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

            productService.updateProduct(productId, productRequest, userId);

            assertNotEquals(originalUpdatedAt, testProduct.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("deleteProduct Tests")
    class DeleteProductTests {

        @Test
        @DisplayName("Should delete product successfully")
        void deleteProduct_Success() {
            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
            doNothing().when(productRepository).delete(testProduct);

            productService.deleteProduct(productId, userId);

            verify(productRepository, times(1)).delete(testProduct);
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void deleteProduct_NotFound_ThrowsException() {
            when(productRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> productService.deleteProduct("nonexistent", userId));
        }

        @Test
        @DisplayName("Should throw exception when user is not owner")
        void deleteProduct_NotOwner_ThrowsException() {
            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

            assertThrows(UnauthorizedException.class,
                    () -> productService.deleteProduct(productId, "other-user"));
        }

        @Test
        @DisplayName("Should publish Kafka event on delete")
        void deleteProduct_PublishesKafkaEvent() {
            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
            doNothing().when(productRepository).delete(testProduct);

            productService.deleteProduct(productId, userId);

            verify(kafkaTemplate, times(1)).send(eq("product.deleted"), anyString());
        }
    }

    @Nested
    @DisplayName("removeMediaFromProduct Tests")
    class RemoveMediaFromProductTests {

        @Test
        @DisplayName("Should remove media ID from product")
        void removeMediaFromProduct_Success() {
            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            productService.removeMediaFromProduct(productId, "media-1");

            assertFalse(testProduct.getMediaIds().contains("media-1"));
            verify(productRepository, times(1)).save(testProduct);
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void removeMediaFromProduct_NotFound_ThrowsException() {
            when(productRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class,
                    () -> productService.removeMediaFromProduct("nonexistent", "media-1"));
        }
    }

    @Nested
    @DisplayName("deleteProductsByUserId Tests")
    class DeleteProductsByUserIdTests {

        @Test
        @DisplayName("Should delete all products for user")
        void deleteProductsByUserId_Success() {
            Product product2 = Product.builder()
                    .id("product-789")
                    .name("Product 2")
                    .userId(userId)
                    .mediaIds(new ArrayList<>())
                    .build();

            when(productRepository.findByUserId(userId)).thenReturn(Arrays.asList(testProduct, product2));
            doNothing().when(productRepository).delete(any(Product.class));

            productService.deleteProductsByUserId(userId);

            verify(productRepository, times(2)).delete(any(Product.class));
            verify(kafkaTemplate, times(2)).send(eq("product.deleted"), anyString());
        }

        @Test
        @DisplayName("Should handle user with no products")
        void deleteProductsByUserId_NoProducts() {
            when(productRepository.findByUserId(userId)).thenReturn(new ArrayList<>());

            productService.deleteProductsByUserId(userId);

            verify(productRepository, never()).delete(any(Product.class));
        }
    }
}
