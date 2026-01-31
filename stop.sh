#!/bin/bash

# Financial Journal Maker - Stop Script
# This script stops all running services

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Stopping Financial Journal Maker services...${NC}"

# Check if docker-compose is available
if ! command -v docker-compose &> /dev/null; then
    COMPOSE_CMD="docker compose"
else
    COMPOSE_CMD="docker-compose"
fi

# Stop Docker services
if docker info &> /dev/null; then
    echo -e "${YELLOW}Stopping Docker containers...${NC}"
    $COMPOSE_CMD down
    echo -e "${GREEN}Docker containers stopped${NC}"
else
    echo -e "${YELLOW}Docker is not running, skipping container shutdown${NC}"
fi

# Kill any Java processes running the application
echo -e "${YELLOW}Checking for running Java processes...${NC}"
JAVA_PID=$(ps aux | grep "coa-management.*\.jar" | grep -v grep | awk '{print $2}')
if [ -n "$JAVA_PID" ]; then
    echo -e "${YELLOW}Stopping Java application (PID: $JAVA_PID)...${NC}"
    kill $JAVA_PID
    echo -e "${GREEN}Java application stopped${NC}"
else
    echo -e "${GREEN}No running Java application found${NC}"
fi

echo -e "\n${GREEN}All services stopped successfully${NC}"
