package ax.gritlab.buy_01.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import ax.gritlab.buy.model.Media;

public interface MediaRepository extends MongoRepository<Media, String> {
}