#!/bin/bash

echo "🔄 Restoring all test changes..."

# Restore docker-compose
if [ -f deployment/docker-compose.yml.backup ]; then
    cp deployment/docker-compose.yml.backup deployment/docker-compose.yml
    echo "✅ docker-compose.yml restored"
fi

# Remove failing test
if [ -f user-service/src/test/java/ax/gritlab/buy01/user/FailingTest.java ]; then
    rm user-service/src/test/java/ax/gritlab/buy01/user/FailingTest.java
    echo "✅ Failing test removed"
fi

echo "✅ All test changes restored"
