#!/bin/bash

# Financial Journal Maker - Local Development Startup Script
# This script starts the application locally without Docker

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Financial Journal Maker - Local Startup${NC}"
echo -e "${GREEN}========================================${NC}"

# Check if Java 17 is available
echo -e "\n${YELLOW}Checking Java version...${NC}"
if ! command -v java &> /dev/null; then
    echo -e "${RED}Error: Java is not installed${NC}"
    exit 1
fi

# Set JAVA_HOME to Java 17
export JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null || echo "")
if [ -z "$JAVA_HOME" ]; then
    echo -e "${RED}Error: Java 17 is not installed${NC}"
    echo -e "${YELLOW}Please install Java 17 (e.g., Amazon Corretto 17)${NC}"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
echo -e "${GREEN}Using Java version: $JAVA_VERSION${NC}"
echo -e "${GREEN}JAVA_HOME: $JAVA_HOME${NC}"

# Check if PostgreSQL is running
echo -e "\n${YELLOW}Checking PostgreSQL...${NC}"
if ! command -v psql &> /dev/null; then
    echo -e "${YELLOW}Warning: psql command not found${NC}"
    echo -e "${YELLOW}Make sure PostgreSQL is running via Docker or locally${NC}"
else
    if pg_isready -h localhost -p 5432 &> /dev/null; then
        echo -e "${GREEN}PostgreSQL is running${NC}"
    else
        echo -e "${YELLOW}PostgreSQL is not running on localhost:5432${NC}"
        echo -e "${YELLOW}Starting PostgreSQL via Docker...${NC}"
        docker-compose up -d postgres
        echo -e "${YELLOW}Waiting for PostgreSQL to be ready...${NC}"
        sleep 5
    fi
fi

# Build the application
echo -e "\n${YELLOW}Building the application...${NC}"
cd backend
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo -e "${RED}Build failed!${NC}"
    exit 1
fi

echo -e "${GREEN}Build successful!${NC}"

# Start the application
echo -e "\n${YELLOW}Starting the application...${NC}"
echo -e "${GREEN}Application will be available at: http://localhost:8080${NC}"
echo -e "${GREEN}API Documentation: http://localhost:8080/swagger-ui.html${NC}"
echo -e "${GREEN}Health Check: http://localhost:8080/actuator/health${NC}"
echo -e "\n${YELLOW}Press Ctrl+C to stop the application${NC}\n"

java -jar target/coa-management-1.0.0-SNAPSHOT.jar \
    --spring.profiles.active=local \
    --spring.datasource.url=jdbc:postgresql://localhost:5432/coa_db \
    --spring.datasource.username=coa_user \
    --spring.datasource.password=coa_password
