@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set MAVEN_PROJECTBASEDIR=%DIRNAME%

set MAVEN_HOME=%TEMP%\apache-maven-3.9.6
if exist "%MAVEN_HOME%\bin\mvn.cmd" (
    call "%MAVEN_HOME%\bin\mvn.cmd" %*
) else (
    echo Downloading portable Maven...
    powershell -Command "Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip' -OutFile '%TEMP%\apache-maven-3.9.6-bin.zip'; Expand-Archive -Path '%TEMP%\apache-maven-3.9.6-bin.zip' -DestinationPath '%TEMP%' -Force"
    call "%MAVEN_HOME%\bin\mvn.cmd" %*
)
