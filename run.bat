@echo off
rem Starts DAPAS. Run build.bat first.
java --enable-native-access=ALL-UNNAMED -cp "bin;lib\*" Main
