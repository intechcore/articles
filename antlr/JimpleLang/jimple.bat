@echo off

set "SCRIPT_DIR=%~dp0"

java -Xms100M -Xmx500M -cp %SCRIPT_DIR%\target\jimple-jar-with-dependencies.jar org.jimple.interpreter.MainApp %*
