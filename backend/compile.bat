@echo off
chcp 65001 >nul
echo Using Aliyun mirror to compile project...
echo.

REM Set Maven parameters
set MAVEN_OPTS=-Xmx1024m -Dfile.encoding=UTF-8 -Dproject.build.sourceEncoding=UTF-8

REM Use simple settings.xml file
mvn clean compile -s settings-simple.xml -Dmaven.test.skip=true -Dfile.encoding=UTF-8

echo.
echo Compilation completed!
pause