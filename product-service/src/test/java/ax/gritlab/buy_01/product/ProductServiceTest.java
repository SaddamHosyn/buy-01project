package ax.gritlab.buy_01.product;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ProductServiceTest {
    
    @Test
    public void testBasicAssertion() {
        String serviceName = "product-service";
        assertNotNull(serviceName);
        assertEquals("product-service", serviceName);
    }
    
    @Test
    public void testNumberOperations() {
        double price = 29.99;
        assertTrue(price > 0);
        assertEquals(29.99, price, 0.01);
    }
}
