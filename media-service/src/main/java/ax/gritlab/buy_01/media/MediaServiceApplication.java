package ax.gritlab.buy_01.media;

import ax.gritlab.buy_01.media.config.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Main application class for the Media Service.
 */
@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public final class MediaServiceApplication {
    /**
     * Private constructor to prevent instantiation.
     */
    private MediaServiceApplication() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Main method to start the application.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(MediaServiceApplication.class, args);
    }
}
