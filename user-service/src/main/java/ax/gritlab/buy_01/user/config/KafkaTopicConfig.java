package ax.gritlab.buy_01.user.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka topic configuration for user events.
 */
@Configuration
public final class KafkaTopicConfig {
   /**
    * Creates Kafka topic for user deletion events.
    *
    * @return configured NewTopic for user.deleted
    */
   @Bean
   public NewTopic userDeletedTopic() {
      return TopicBuilder.name("user.deleted")
            .partitions(1)
            .replicas(1)
            .build();
   }
}
