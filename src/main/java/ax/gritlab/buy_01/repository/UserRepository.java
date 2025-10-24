package ax.gritlab.buy_01.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import ax.gritlab.buy_01.model.User;

public interface UserRepository extends MongoRepository<User, String> {
}