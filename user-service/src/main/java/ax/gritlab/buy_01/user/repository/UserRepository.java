package ax.gritlab.buy_01.user.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ax.gritlab.buy_01.user.model.User;

public interface UserRepository extends MongoRepository<User, String> {
}
