@echo off
echo Updating Gradle Wrapper to 8.5...
echo.

REM Delete old wrapper jar
if exist gradle\wrapper\gradle-wrapper.jar (
    echo Deleting old gradle-wrapper.jar...
    del /f gradle\wrapper\gradle-wrapper.jar
)

REM Download new wrapper jar
echo Downloading Gradle 8.5 wrapper jar...
powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/gradle/gradle/v8.5.0/gradle/wrapper/gradle-wrapper.jar' -OutFile 'gradle/wrapper/gradle-wrapper.jar'"

REM Check if download was successful
if exist gradle\wrapper\gradle-wrapper.jar (
    echo.
    echo Gradle Wrapper updated successfully!
    echo You can now run: gradlew.bat build
) else (
    echo.
    echo Failed to download Gradle Wrapper JAR
    echo Please check your internet connection and try again
)

echo.
pause
