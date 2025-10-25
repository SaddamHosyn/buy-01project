package ax.gritlab.buy_01.media.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ax.gritlab.buy_01.media.model.Media;

public interface MediaRepository extends MongoRepository<Media, String> {
}
