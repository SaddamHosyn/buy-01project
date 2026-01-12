package ax.gritlab.buy_01.user.repository;

import ax.gritlab.buy_01.user.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Repository interface for User entity.
 * Provides database operations for User management.
 */
public interface UserRepository extends MongoRepository<User, String> {
    /**
     * Find a user by email address.
     *
     * @param email the email address to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);
}
