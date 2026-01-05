package ax.gritlab.buy_01.media.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for RestTemplate bean.
 */
@Configuration
public final class RestTemplateConfig {

   /**
    * Creates a RestTemplate bean for making HTTP requests.
    *
    * @return configured RestTemplate
    */
   @Bean
   public RestTemplate restTemplate() {
      return new RestTemplate();
   }
}
