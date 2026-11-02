@echo off
rem Builds, then runs all tests. The tests use temporary databases, never your real one.
call build.bat
if errorlevel 1 exit /b 1
javac -encoding UTF-8 -cp "bin;lib\*" -d bin test\TestRunner.java
if errorlevel 1 exit /b 1
java -cp "bin;lib\*" TestRunner
if errorlevel 1 exit /b 1
echo.
echo --- restart test: two separate program runs ---
java -cp "bin;lib\*" TestRunner write
java -cp "bin;lib\*" TestRunner read
