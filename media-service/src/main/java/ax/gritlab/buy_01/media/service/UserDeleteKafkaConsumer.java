package ax.gritlab.buy_01.media.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

/**
 * Kafka consumer for user deletion events.
 */
@Component
@RequiredArgsConstructor
public class UserDeleteKafkaConsumer {
    /**
     * Media service for handling media operations.
     */
    @Autowired
    private final MediaService mediaService;

    /**
     * Consumes user deletion events and deletes associated media.
     *
     * @param userId the ID of the deleted user
     */
    @KafkaListener(topics = "user.deleted", groupId = "media-service-group")
    public void consumeUserDeleted(final String userId) {
        System.out.println("Received user deletion event for ID: "
                + userId);
        mediaService.deleteMediaByUserId(userId);
    }
}
