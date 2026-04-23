@echo off
setlocal enabledelayedexpansion

echo ========================================
echo MQTT 功能更新 - 构建测试
echo ========================================
echo.

echo [1/4] 清理旧的构建文件...
call .\gradlew.bat clean
if !ERRORLEVEL! NEQ 0 (
    echo [错误] 清理失败
    pause
    exit /b 1
)
echo [完成] 清理成功
echo.

echo [2/4] 同步 Gradle 依赖...
call .\gradlew.bat --refresh-dependencies
if !ERRORLEVEL! NEQ 0 (
    echo [警告] 依赖同步可能有问题，继续构建...
)
echo [完成] 依赖同步完成
echo.

echo [3/4] 构建 Debug APK...
call .\gradlew.bat assembleDebug
if !ERRORLEVEL! NEQ 0 (
    echo [错误] 构建失败
    echo.
    echo 可能的原因：
    echo 1. 网络问题导致依赖下载失败
    echo 2. JDK 版本不兼容
    echo 3. Android SDK 未正确配置
    echo.
    echo 请检查错误信息并重试
    pause
    exit /b 1
)
echo [完成] 构建成功
echo.

echo [4/4] 验证 APK 文件...
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo [完成] APK 文件已生成
    echo.
    echo ========================================
    echo 构建测试成功！
    echo ========================================
    echo.
    echo APK 位置: app\build\outputs\apk\debug\app-debug.apk
    echo.
    
    for %%A in ("app\build\outputs\apk\debug\app-debug.apk") do (
        echo APK 大小: %%~zA 字节
    )
    echo.
    
    echo 下一步操作：
    echo 1. 连接 Android 设备或启动模拟器
    echo 2. 运行: adb install -r app\build\outputs\apk\debug\app-debug.apk
    echo 3. 或者直接运行: build-apk.bat 并选择安装
    echo.
    
    set /p open="是否打开 APK 所在文件夹？(y/n): "
    if /i "!open!"=="y" (
        explorer app\build\outputs\apk\debug
    )
    echo.
    
    set /p install="是否立即安装到连接的设备？(y/n): "
    if /i "!install!"=="y" (
        echo.
        echo 正在安装...
        adb install -r app\build\outputs\apk\debug\app-debug.apk
        if !ERRORLEVEL! EQU 0 (
            echo.
            echo [完成] 安装成功！
            echo.
            echo 可以在设备上启动应用进行测试了
        ) else (
            echo.
            echo [错误] 安装失败
            echo 请确保：
            echo 1. 设备已连接并开启 USB 调试
            echo 2. 运行 'adb devices' 确认设备已识别
        )
    )
) else (
    echo [错误] APK 文件未找到
    echo 构建可能未成功完成
)

echo.
echo ========================================
echo 测试完成
echo ========================================
echo.
pause
