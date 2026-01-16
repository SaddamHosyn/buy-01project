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
        // Intentional failure to trigger pipeline rollback demo
        assertEquals(1, 2, "Forcing backend test failure to verify rollback");
    }
}
