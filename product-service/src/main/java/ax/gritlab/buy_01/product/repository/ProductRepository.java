package ax.gritlab.buy_01.product.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ax.gritlab.buy_01.product.model.Product;

/**
 * Repository for Product entities.
 */
public interface ProductRepository
        extends MongoRepository<Product, String> {

    /**
     * Delete all products for a user.
     *
     * @param userId the user ID
     */
    void deleteByUserId(String userId);

    /**
     * Find all products for a user.
     *
     * @param userId the user ID
     * @return list of products
     */
    java.util.List<Product> findByUserId(String userId);
}

