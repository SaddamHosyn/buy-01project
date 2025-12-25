#!/bin/bash

echo "🧪 ROLLBACK TEST - Intentionally breaking deployment"
echo "=================================================="

cd deployment

# Backup current docker-compose
cp docker-compose.yml docker-compose.yml.backup

# Break the docker-compose by adding invalid syntax
cat >> docker-compose.yml << 'EOF'

  # INTENTIONAL ERROR FOR TESTING
  broken-service:
    image: non-existent-image:broken
    container_name: this-will-fail
    ports:
      - "99999:99999"  # Invalid port
EOF

echo "✅ docker-compose.yml corrupted for testing"
echo "Now run Jenkins build - it will fail and trigger rollback"
echo ""
echo "To restore: cp docker-compose.yml.backup docker-compose.yml"
echo "=================================================="
