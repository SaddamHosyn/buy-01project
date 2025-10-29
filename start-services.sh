# To run:
# chmod +x start-services.sh
# ./start-services.sh

#!/bin/bash

# Start all services in parallel
mvn -f service-registry spring-boot:run &
mvn -f user-service spring-boot:run &
mvn -f product-service spring-boot:run &
mvn -f media-service spring-boot:run &
mvn -f api-gateway spring-boot:run &

# Wait for all background processes to finish
wait