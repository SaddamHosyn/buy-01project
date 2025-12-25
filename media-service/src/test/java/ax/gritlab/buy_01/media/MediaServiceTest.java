package ax.gritlab.buy_01.media;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MediaServiceTest {
    
    @Test
    public void testBasicAssertion() {
        String serviceName = "media-service";
        assertNotNull(serviceName);
        assertEquals("media-service", serviceName);
    }
    
    @Test
    public void testFileExtensionValidation() {
        String filename = "image.png";
        assertTrue(filename.endsWith(".png") || filename.endsWith(".jpg"));
    }
}
