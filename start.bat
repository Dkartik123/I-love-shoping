@echo off
REM I Love Shopping - Windows Startup Script
REM This script builds and runs the entire application with Docker

echo.
echo 🛒 I Love Shopping - Starting Application
echo ==========================================
echo.

REM Check if Docker is running
docker info >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker is not running. Please start Docker Desktop and try again.
    pause
    exit /b 1
)

REM Check if .env file exists
if not exist .env (
    echo 📝 Creating .env file from template...
    copy .env.example .env
    echo ⚠️  Please edit .env file with your configuration before running in production.
)

REM Parse arguments
set BUILD_ONLY=false
set DETACHED=true

:parse_args
if "%~1"=="" goto end_parse
if "%~1"=="--build" set BUILD_ONLY=true
if "%~1"=="--foreground" set DETACHED=false
if "%~1"=="-f" set DETACHED=false
if "%~1"=="--help" goto show_help
if "%~1"=="-h" goto show_help
shift
goto parse_args
:end_parse

REM Build images
echo.
echo 🔨 Building Docker images...
docker-compose build

if "%BUILD_ONLY%"=="true" (
    echo.
    echo ✅ Build complete!
    pause
    exit /b 0
)

REM Start containers
echo.
echo 🚀 Starting containers...

if "%DETACHED%"=="true" (
    docker-compose up -d
    
    echo.
    echo ⏳ Waiting for services to be healthy...
    
    REM Wait for backend
    set RETRIES=30
    :wait_loop
    if %RETRIES% LEQ 0 goto backend_failed
    
    docker-compose exec -T backend wget --quiet --tries=1 --spider http://localhost:8080/actuator/health >nul 2>&1
    if errorlevel 1 (
        echo Waiting for backend... (%RETRIES% attempts remaining)
        set /a RETRIES-=1
        timeout /t 5 /nobreak >nul
        goto wait_loop
    )
    
    goto backend_ready
    
    :backend_failed
    echo ❌ Backend failed to start. Check logs with: docker-compose logs backend
    pause
    exit /b 1
    
    :backend_ready
    echo.
    echo ✅ Application is running!
    echo.
    echo 📍 Access the application:
    echo    Frontend: http://localhost
    echo    Backend API: http://localhost:8080
    echo    API Docs: http://localhost:8080/swagger-ui.html
    echo.
    echo 📋 Useful commands:
    echo    View logs: docker-compose logs -f
    echo    Stop: docker-compose down
    echo    Restart: docker-compose restart
    echo.
    pause
) else (
    docker-compose up
)

exit /b 0

:show_help
echo Usage: start.bat [options]
echo.
echo Options:
echo   --build       Build images only, don't start containers
echo   --foreground  Run in foreground (see logs)
echo   --help        Show this help message
pause
exit /b 0
