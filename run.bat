@echo off
title Smart Hostel Backend Runner
echo =====================================================================
echo           SMART HOSTEL COMPLAINT ^& MANAGEMENT SYSTEM RUNNER
echo =====================================================================
echo.
echo Please replace the placeholder credentials below with your real Neon details
echo before running the script.
echo.

:: --- CONFIGURE YOUR NEON POSTGRESQL CREDENTIALS HERE ---
set DB_URL=jdbc:postgresql://ep-broad-mode-aqw37f7q-pooler.c-8.us-east-1.aws.neon.tech/neondb?sslmode=require
set DB_USERNAME=neondb_owner
set DB_PASSWORD=npg_6hQszRK3AtTr
:: -------------------------------------------------------

echo Connecting to database: %DB_URL%
echo Username: %DB_USERNAME%
echo.
echo Starting Spring Boot application on port 9001...
echo.

mvn spring-boot:run

echo.
echo Application stopped.
pause
