@echo off
call build.bat
if %ERRORLEVEL% == 0 (
    echo Starting Oops UML Editor...
    java -cp out oops.UMLEditor
)
