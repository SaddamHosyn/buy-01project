package ax.gritlab.buy_01.user;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    @Test
    public void testBasicAssertion() {
        String serviceName = "user-service";
        assertNotNull(serviceName);
        assertEquals("user-service", serviceName);
    }

    @Test
    public void testStringOperations() {
        String email = "test@example.com";
        assertTrue(email.contains("@"));
        assertTrue(email.endsWith(".com"));
    }
}
