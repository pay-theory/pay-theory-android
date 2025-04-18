@echo off
setlocal

REM Set JAVA_HOME to JDK 17 for this batch file
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
set PATH=%JAVA_HOME%\bin;%PATH%

REM Launch Cursor with JDK 17 environment
start cursor.cmd %CD%

endlocal 