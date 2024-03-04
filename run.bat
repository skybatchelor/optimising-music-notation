@echo off

:: change the directory to the directory of the script
cd /D "%~dp0"
"%JAVA_HOME%/bin/java.exe" -jar optimisingmusicnotation.jar %1 %2 > OUTPUT 2> ERRORS

:: ERRORS will always be created
:: if it is empty then create the PASS file
:: else create FAIL file
:: Sibelius will be watching for one of these files
for /f %%i in ("ERRORS") do set size=%%~zi
if %size% gtr 0 (echo FAIL > FAIL) else (echo PASS > PASS)

:: loop over each line in OUTPUT and open it in the default software
:: each line will be a generated pdf
for /F delims^=^ eol^= %%i in ('type OUTPUT') do start "" "%%i"
