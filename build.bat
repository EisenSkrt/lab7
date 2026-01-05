
@echo off
echo Building modules...
call mvn -q -DskipTests clean package
if %errorlevel% neq 0 exit /b %errorlevel%

if not exist dist mkdir dist
copy server\target\server.jar dist\server.jar >nul
copy client\target\client.jar dist\client.jar >nul

echo Done. JARs are in dist\
