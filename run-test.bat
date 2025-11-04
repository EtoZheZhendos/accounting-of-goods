@echo off
chcp 65001 >nul
echo ======================================
echo  РўРµСЃС‚РѕРІРѕРµ РєРѕРЅСЃРѕР»СЊРЅРѕРµ РїСЂРёР»РѕР¶РµРЅРёРµ
echo ======================================
echo.

echo [1/2] РљРѕРјРїРёР»СЏС†РёСЏ РїСЂРѕРµРєС‚Р°...
call mvn clean compile
if %errorlevel% neq 0 (
    echo.
    echo [РћРЁРР‘РљРђ] РќРµ СѓРґР°Р»РѕСЃСЊ СЃРєРѕРјРїРёР»РёСЂРѕРІР°С‚СЊ РїСЂРѕРµРєС‚!
    pause
    exit /b %errorlevel%
)

echo.
echo [2/2] Р—Р°РїСѓСЃРє С‚РµСЃС‚РѕРІРѕРіРѕ РїСЂРёР»РѕР¶РµРЅРёСЏ...
echo.
call mvn exec:java "-Dexec.mainClass=com.store.inventory.TestBackend"

pause
