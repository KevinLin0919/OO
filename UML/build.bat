@echo off
echo Compiling Oops UML Editor...
if not exist out mkdir out
javac -encoding UTF-8 -d out -sourcepath src src\oops\UMLEditor.java
if %ERRORLEVEL% == 0 (
    echo Build successful!
) else (
    echo Build failed!
    pause
)
