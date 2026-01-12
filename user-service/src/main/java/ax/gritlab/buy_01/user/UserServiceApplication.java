package ax.gritlab.buy_01.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for user microservice.
 */
@SpringBootApplication
public final class UserServiceApplication {

    /**
     * Private constructor to prevent instantiation.
     */
    private UserServiceApplication() {
    }

    /**
     * Main entry point for the application.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
