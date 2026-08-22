@echo off
setlocal
cd /d "%~dp0"

echo ========================================
echo   SmartLibrary - Compile and Run
echo ========================================
echo.

if not exist out mkdir out

echo [1/2] Compiling Java sources...
javac -Xlint:all -d out src\app\*.java src\core\*.java src\dsa\*.java src\model\*.java src\ui\*.java
if errorlevel 1 (
    echo.
    echo Compilation failed. Make sure Java JDK is installed and on your PATH.
    echo Download: https://adoptium.net/
    pause
    exit /b 1
)

echo [2/2] Starting SmartLibrary...
echo.
java -cp out Main

echo.
echo SmartLibrary closed.
pause
