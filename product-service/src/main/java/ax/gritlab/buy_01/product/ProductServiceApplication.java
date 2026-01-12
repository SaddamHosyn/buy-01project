package ax.gritlab.buy_01.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Product Service.
 */
@SpringBootApplication
public final class ProductServiceApplication {

    /**
     * Private constructor to prevent instantiation.
     */
    private ProductServiceApplication() {
        throw new UnsupportedOperationException(
                "Utility class cannot be instantiated");
    }

    /**
     * Main method to start the Product Service application.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
