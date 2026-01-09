#!/bin/bash

# I Love Shopping - Startup Script
# This script builds and runs the entire application with Docker

set -e

echo "🛒 I Love Shopping - Starting Application"
echo "=========================================="

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if .env file exists
if [ ! -f .env ]; then
    echo "📝 Creating .env file from template..."
    cp .env.example .env
    echo "⚠️  Please edit .env file with your configuration before running in production."
fi

# Parse arguments
BUILD_ONLY=false
DETACHED=true

while [[ "$#" -gt 0 ]]; do
    case $1 in
        --build) BUILD_ONLY=true ;;
        --foreground|-f) DETACHED=false ;;
        --help|-h)
            echo "Usage: ./start.sh [options]"
            echo ""
            echo "Options:"
            echo "  --build       Build images only, don't start containers"
            echo "  --foreground  Run in foreground (see logs)"
            echo "  --help        Show this help message"
            exit 0
            ;;
        *) echo "Unknown parameter: $1"; exit 1 ;;
    esac
    shift
done

# Build images
echo ""
echo "🔨 Building Docker images..."
docker-compose build

if [ "$BUILD_ONLY" = true ]; then
    echo ""
    echo "✅ Build complete!"
    exit 0
fi

# Start containers
echo ""
echo "🚀 Starting containers..."

if [ "$DETACHED" = true ]; then
    docker-compose up -d
    
    echo ""
    echo "⏳ Waiting for services to be healthy..."
    
    # Wait for backend to be healthy
    RETRIES=30
    until docker-compose exec -T backend wget --quiet --tries=1 --spider http://localhost:8080/actuator/health 2>/dev/null || [ $RETRIES -eq 0 ]; do
        echo "Waiting for backend... ($RETRIES attempts remaining)"
        RETRIES=$((RETRIES-1))
        sleep 5
    done
    
    if [ $RETRIES -eq 0 ]; then
        echo "❌ Backend failed to start. Check logs with: docker-compose logs backend"
        exit 1
    fi
    
    echo ""
    echo "✅ Application is running!"
    echo ""
    echo "📍 Access the application:"
    echo "   Frontend: http://localhost"
    echo "   Backend API: http://localhost:8080"
    echo "   API Docs: http://localhost:8080/swagger-ui.html"
    echo ""
    echo "📋 Useful commands:"
    echo "   View logs: docker-compose logs -f"
    echo "   Stop: docker-compose down"
    echo "   Restart: docker-compose restart"
else
    docker-compose up
fi
