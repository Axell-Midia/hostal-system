@echo off
echo Deteniendo Sistema Hostal...
taskkill /FI "WINDOWTITLE eq Backend Hostal*" /F 2>nul
taskkill /FI "WINDOWTITLE eq Frontend Hostal*" /F 2>nul
echo Done.
pause
