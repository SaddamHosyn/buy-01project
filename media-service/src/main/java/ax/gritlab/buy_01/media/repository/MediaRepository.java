package ax.gritlab.buy_01.media.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ax.gritlab.buy_01.media.model.Media;

import java.util.List;

/**
 * Repository interface for Media entity operations.
 */
public interface MediaRepository extends MongoRepository<Media, String> {
    /**
     * Find all media by user ID.
     *
     * @param userId the user ID
     * @return list of media
     */
    List<Media> findByUserId(String userId);

    /**
     * Find all media by product ID.
     *
     * @param productId the product ID
     * @return list of media
     */
    List<Media> findByProductId(String productId);

    /**
     * Delete all media by product ID.
     *
     * @param productId the product ID
     */
    void deleteByProductId(String productId);
}
