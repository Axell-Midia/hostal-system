@echo off
echo ========================================
echo   Sistema Hostal - Iniciando...
echo ========================================

echo.
echo [1/2] Iniciando Backend (Spring Boot)...
start "Backend Hostal" cmd /k "cd /d C:\Users\Alexander\hostal-system\backend && mvn spring-boot:run"

echo [2/2] Iniciando Frontend (Vite)...
start "Frontend Hostal" cmd /k "cd /d C:\Users\Alexander\hostal-system\frontend && npm run dev"

echo.
echo Espera 10 segundos y abre: http://localhost:5173
echo.
pause
