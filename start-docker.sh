#!/bin/bash

# Financial Journal Maker - Docker Startup Script
# This script starts the entire application stack using Docker Compose

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Financial Journal Maker - Docker Startup${NC}"
echo -e "${GREEN}========================================${NC}"

# Check if Docker is running
echo -e "\n${YELLOW}Checking Docker...${NC}"
if ! docker info &> /dev/null; then
    echo -e "${RED}Error: Docker is not running${NC}"
    echo -e "${YELLOW}Please start Docker Desktop and try again${NC}"
    exit 1
fi

echo -e "${GREEN}Docker is running${NC}"

# Check if docker-compose is available
if ! command -v docker-compose &> /dev/null; then
    echo -e "${YELLOW}docker-compose not found, using 'docker compose' instead${NC}"
    COMPOSE_CMD="docker compose"
else
    COMPOSE_CMD="docker-compose"
fi

# Stop any existing containers
echo -e "\n${YELLOW}Stopping existing containers...${NC}"
$COMPOSE_CMD down

# Build and start services
echo -e "\n${YELLOW}Building and starting services...${NC}"
$COMPOSE_CMD up --build -d

# Wait for services to be healthy
echo -e "\n${YELLOW}Waiting for services to be ready...${NC}"
sleep 5

# Check PostgreSQL health
echo -e "${YELLOW}Checking PostgreSQL...${NC}"
for i in {1..30}; do
    if docker exec coa-postgres pg_isready -U coa_user -d coa_db &> /dev/null; then
        echo -e "${GREEN}PostgreSQL is ready!${NC}"
        break
    fi
    if [ $i -eq 30 ]; then
        echo -e "${RED}PostgreSQL failed to start${NC}"
        $COMPOSE_CMD logs postgres
        exit 1
    fi
    sleep 1
done

# Check application health
echo -e "${YELLOW}Checking application...${NC}"
for i in {1..60}; do
    if curl -f http://localhost:8080/actuator/health &> /dev/null; then
        echo -e "${GREEN}Application is ready!${NC}"
        break
    fi
    if [ $i -eq 60 ]; then
        echo -e "${RED}Application failed to start${NC}"
        $COMPOSE_CMD logs coa-service
        exit 1
    fi
    sleep 1
done

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}All services are running!${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Application: http://localhost:8080${NC}"
echo -e "${GREEN}API Documentation: http://localhost:8080/swagger-ui.html${NC}"
echo -e "${GREEN}Health Check: http://localhost:8080/actuator/health${NC}"
echo -e "${GREEN}PostgreSQL: localhost:5432${NC}"
echo -e "\n${YELLOW}To view logs: $COMPOSE_CMD logs -f${NC}"
echo -e "${YELLOW}To stop: $COMPOSE_CMD down${NC}"
echo -e "${YELLOW}To stop and remove data: $COMPOSE_CMD down -v${NC}\n"
