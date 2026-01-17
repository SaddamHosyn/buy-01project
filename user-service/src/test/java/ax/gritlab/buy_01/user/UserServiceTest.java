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

    @Test
    public void testForcedBackendFailureForRollbackDemo() {
        // Intentionally fail this test for rollback demo
        assertEquals(1, 2, "This failure should trigger rollback");
    }
}
