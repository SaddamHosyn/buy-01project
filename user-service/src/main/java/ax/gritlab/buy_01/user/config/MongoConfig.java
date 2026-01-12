package ax.gritlab.buy_01.user.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import ax.gritlab.buy_01.user.model.User;

/**
 * MongoDB configuration for index initialization.
 */
@Configuration
@RequiredArgsConstructor
public final class MongoConfig {

    /** MongoDB template for database operations. */
    private final MongoTemplate mongoTemplate;
    /** MongoDB mapping context for entity mappings. */
    private final MongoMappingContext mongoMappingContext;

    /**
     * Initializes MongoDB indexes after bean construction.
     */
    @PostConstruct
    public void initIndexes() {
        IndexOperations indexOps = mongoTemplate.indexOps(User.class);
        IndexResolver resolver =
                new MongoPersistentEntityIndexResolver(
                        mongoMappingContext);
        resolver.resolveIndexFor(User.class)
                .forEach(indexOps::ensureIndex);
    }
}
