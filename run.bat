@echo off
title SaferPath Application
echo Compiling SaferPath Application...

:: Clean previous compilation
if exist src\*.class del src\*.class

:: Compile the app entry file only; the project contains multiple legacy fragment files
javac -d . src\SaferPathWebApp.java

if %errorlevel% neq 0 (
    echo.
    echo Compilation failed!
    pause
    exit /b 1
)

echo.
echo ✅ Compilation successful!
echo.
echo Starting SaferPath Application...
echo 🌐 Server will be available at: http://localhost:8080
echo 🛑 Press Ctrl+C to stop the server
echo.

:: Run the application
java SaferPathWebApp

pause