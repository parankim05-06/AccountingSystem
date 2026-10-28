@echo off
rem Compiles the whole project into the bin folder.
if not exist bin mkdir bin
javac -encoding UTF-8 -cp "lib\*" -d bin src\Main.java src\database\*.java src\model\*.java src\dao\*.java src\service\*.java src\ui\*.java
if errorlevel 1 (
    echo.
    echo BUILD FAILED - read the error messages above.
    exit /b 1
)
echo BUILD OK
