package ax.gritlab.buy_01.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import ax.gritlab.buy.model.User;

public interface UserRepository extends MongoRepository<User, String> {
}