package ax.gritlab.buy_01.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final MongoTemplate mongoTemplate;

    public DataInitializer(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        String collectionName = "product";

        // First, check if the collection already exists
        if (!mongoTemplate.collectionExists(collectionName)) {
            // If it doesn't exist, create it immediately.
            mongoTemplate.createCollection(collectionName);
            System.out.println("Successfully created empty collection: " + collectionName);
        } else {
            System.out.println("Collection already exists: " + collectionName);
        }
    }
}