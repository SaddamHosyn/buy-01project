#!/bin/bash

echo "🧪 ERROR HANDLING TEST - Creating failing test"
echo "=============================================="

# Create a failing test in user-service
mkdir -p user-service/src/test/java/ax/gritlab/buy01/user

cat > user-service/src/test/java/ax/gritlab/buy01/user/FailingTest.java << 'EOF'
package ax.gritlab.buy01.user;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FailingTest {
    
    @Test
    public void testIntentionalFailure() {
        System.out.println("This test intentionally fails for demonstration");
        fail("Intentional failure to test pipeline error handling");
    }
}
EOF

echo "✅ Failing test created at user-service/src/test/java/ax/gritlab/buy01/user/FailingTest.java"
echo ""
echo "Now run Jenkins build - it will fail at Backend Tests stage"
echo ""
echo "To restore: rm user-service/src/test/java/ax/gritlab/buy01/user/FailingTest.java"
echo "=============================================="
