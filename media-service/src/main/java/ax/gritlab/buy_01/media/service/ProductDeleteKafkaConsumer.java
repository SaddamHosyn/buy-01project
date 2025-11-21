package ax.gritlab.buy_01.media.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductDeleteKafkaConsumer {
    @Autowired
    private final MediaService mediaService;

    @KafkaListener(topics = "product.deleted", groupId = "media-service-group")
    public void consumeProductDeleted(String productId) {
        System.out.println("Received product deletion event for ID: " + productId);
        mediaService.deleteMediaByProductId(productId);
    }
}