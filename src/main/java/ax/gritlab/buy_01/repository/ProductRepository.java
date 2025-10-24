package ax.gritlab.buy_01.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import ax.gritlab.buy.model.Product;

public interface ProductRepository extends MongoRepository<Product, String> {
}