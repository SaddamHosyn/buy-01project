package ax.gritlab.buy_01.media.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductDeleteKafkaConsumer {
    private final MediaService mediaService;

    @KafkaListener(topics = "product.deleted", groupId = "media-service-group")
    public void consumeProductDeleted(String productId) {
        mediaService.deleteMediaByProductId(productId);
    }
}