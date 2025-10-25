package ax.gritlab.buy_01.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import ax.gritlab.buy_01.model.Product;

public interface ProductRepository extends MongoRepository<Product, String> {
}