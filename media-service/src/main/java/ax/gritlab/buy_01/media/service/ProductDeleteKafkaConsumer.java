package ax.gritlab.buy_01.media.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Kafka consumer for product deletion events.
 */
@Component
@RequiredArgsConstructor
public class ProductDeleteKafkaConsumer {
    /**
     * Media service for handling media operations.
     */
    @Autowired
    private final MediaService mediaService;

    /**
     * Object mapper for JSON parsing.
     */
    @Autowired
    private final ObjectMapper objectMapper;

    /**
     * Consumes product deletion events and deletes associated media.
     *
     * @param message the deletion event message
     */
    @KafkaListener(topics = "product.deleted",
                   groupId = "media-service-group")
    public void consumeProductDeleted(final String message) {
        System.out.println("Received product deletion event: "
                           + message);

        try {
            // If producer sends JSON with mediaIds, prefer
            // deleting by explicit media ids
            if (message != null
                && message.trim().startsWith("{")) {
                JsonNode node = objectMapper.readTree(message);
                List<String> mediaIds = new ArrayList<>();
                if (node.has("mediaIds")
                    && node.get("mediaIds").isArray()) {
                    for (JsonNode idNode
                            : node.get("mediaIds")) {
                        mediaIds.add(idNode.asText());
                    }
                }

                if (!mediaIds.isEmpty()) {
                    mediaService.deleteMediaByIds(mediaIds);
                    return;
                }

                // fallback to productId field if present
                if (node.has("id")) {
                    String productId = node.get("id").asText();
                    mediaService
                        .deleteMediaByProductId(productId);
                    return;
                }
            }
        } catch (Exception e) {
            System.err.println(
                "Failed to parse product.deleted message: "
                    + e.getMessage());
        }

        // Fallback: treat the payload as a raw productId string
        mediaService.deleteMediaByProductId(message);
    }
}
