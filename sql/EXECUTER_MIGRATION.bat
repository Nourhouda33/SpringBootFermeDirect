@echo off
echo ========================================
echo MIGRATION COUPONS HYBRIDES
echo ========================================
echo.

REM Chercher PostgreSQL dans les emplacements courants
set PSQL_PATH=

if exist "C:\Program Files\PostgreSQL\16\bin\psql.exe" (
    set PSQL_PATH=C:\Program Files\PostgreSQL\16\bin\psql.exe
)
if exist "C:\Program Files\PostgreSQL\15\bin\psql.exe" (
    set PSQL_PATH=C:\Program Files\PostgreSQL\15\bin\psql.exe
)
if exist "C:\Program Files\PostgreSQL\14\bin\psql.exe" (
    set PSQL_PATH=C:\Program Files\PostgreSQL\14\bin\psql.exe
)
if exist "C:\Program Files\PostgreSQL\13\bin\psql.exe" (
    set PSQL_PATH=C:\Program Files\PostgreSQL\13\bin\psql.exe
)

if "%PSQL_PATH%"=="" (
    echo ERREUR: PostgreSQL n'a pas ete trouve!
    echo.
    echo Veuillez installer pgAdmin ou utiliser une autre methode.
    echo Voir: COMMANDES_BD_COUPONS.txt
    echo.
    pause
    exit /b 1
)

echo PostgreSQL trouve: %PSQL_PATH%
echo.
echo Execution de la migration...
echo.

"%PSQL_PATH%" -U postgres -d ferme_directe -f COUPON_MIGRATION_SIMPLE.sql

echo.
echo ========================================
echo Migration terminee!
echo ========================================
echo.
pause
