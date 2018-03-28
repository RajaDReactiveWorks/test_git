ECHO ----------------------------------------------------------------------
ECHO Maven build for leap-DdlUtils-jdbc
ECHO ----------------------------------------------------------------------
cd %cd%\leap-DdlUtils-jdbc
del *.classpath
del *.project
rmdir /s /q "%cd%/target"
rmdir /s /q "%cd%/.settings"
call mvn clean install -DskipTests
if not "%ERRORLEVEL%" == "0" set /p id="Terminate batch job (Y/N)?"/b
if "%id%"=="Y" exit /b
call mvn eclipse:clean
call mvn eclipse:eclipse

ECHO ----------------------------------------------------------------------
ECHO Maven build for leap-MetaModel-jdbc
ECHO ----------------------------------------------------------------------
cd ..\leap-MetaModel-jdbc
del *.classpath
del *.project
rmdir /s /q "%cd%/target"
rmdir /s /q "%cd%/.settings"
call mvn clean install -DskipTests
if not "%ERRORLEVEL%" == "0" set /p id="Terminate batch job (Y/N)?"/b
if "%id%"=="Y" exit /b
call mvn eclipse:clean
call mvn eclipse:eclipse


ECHO ----------------------------------------------------------------------
ECHO Maven build for leap-MetaModel-cassandra
ECHO ----------------------------------------------------------------------
cd ..\leap-MetaModel-cassandra
del *.classpath
del *.project
rmdir /s /q "%cd%/target"
rmdir /s /q "%cd%/.settings"
call mvn clean install -DskipTests
if not "%ERRORLEVEL%" == "0" set /p id="Terminate batch job (Y/N)?"/b
if "%id%"=="Y" exit /b
call mvn eclipse:clean
call mvn eclipse:eclipse

ECHO ----------------------------------------------------------------------
ECHO Maven build for leap-camel
ECHO ----------------------------------------------------------------------
cd ..\leap-camel
del *.classpath
del *.project
rmdir /s /q "%cd%/.settings"
rmdir /s /q "%cd%/target"
call mvn clean install -DskipTests
if not "%ERRORLEVEL%" == "0" set /p id="Terminate batch job (Y/N)?"/b
if "%id%"=="Y" exit /b
call mvn eclipse:clean
call mvn eclipse:eclipse

ECHO ----------------------------------------------------------------------
ECHO Maven build for leap-token-generator
ECHO ----------------------------------------------------------------------
cd ..\leap-token-generator
del *.classpath
del *.project
rmdir /s /q "%cd%/target"
rmdir /s /q "%cd%/.settings"
call mvn clean install -DskipTests
if not "%ERRORLEVEL%" == "0" set /p id="Terminate batch job (Y/N)?"/b
if "%id%"=="Y" exit /b
call mvn eclipse:clean
call mvn eclipse:eclipse

ECHO ----------------------------------------------------------------------
ECHO Maven build for leap-framework
ECHO ----------------------------------------------------------------------
cd ..\leap-framework
del *.classpath
del *.project
rmdir /s /q "%cd%/target"
rmdir /s /q "%cd%/.settings"
call mvn clean install -DskipTests
if not "%ERRORLEVEL%" == "0" set /p id="Terminate batch job (Y/N)?"/b
if "%id%"=="Y" exit /b
call mvn eclipse:clean
call mvn eclipse:eclipse

ECHO ----------------------------------------------------------------------
ECHO Maven build for leap-core
ECHO ----------------------------------------------------------------------
cd ..\leap-core
del *.classpath
del *.project
rmdir /s /q "%cd%/target"
rmdir /s /q "%cd%/.settings"
call mvn clean install -DskipTests
if not "%ERRORLEVEL%" == "0" set /p id="Terminate batch job (Y/N)?"/b
if "%id%"=="Y" exit /b
call mvn eclipse:clean
call mvn eclipse:eclipse




ECHO ----------------------------------------------------------------------
ECHO Maven build for sample-labelproducer-feature-impl
ECHO ----------------------------------------------------------------------
cd ..\sample-labelproducer-feature-impl
del *.classpath
del *.project
rmdir /s /q "%cd%/target"
rmdir /s /q "%cd%/.settings"
call mvn clean install -DskipTests
if not "%ERRORLEVEL%" == "0" set /p id="Terminate batch job (Y/N)?"/b
if "%id%"=="Y" exit /b
call mvn eclipse:clean
call mvn eclipse:eclipse

ECHO ----------------------------------------------------------------------
ECHO Maven build for sample-labelproducer-feature-exec
ECHO ----------------------------------------------------------------------
cd ..\sample-labelproducer-feature-exec
del *.classpath
del *.project
rmdir /s /q "%cd%/target"
rmdir /s /q "%cd%/.settings"
call mvn clean install -DskipTests
if not "%ERRORLEVEL%" == "0" set /p id="Terminate batch job (Y/N)?"/b
if "%id%"=="Y" exit /b
call mvn eclipse:clean
call mvn eclipse:eclipse



ECHO ----------------------------------------------------------------------
ECHO Maven build for subsciber-printservice-feature-impl
ECHO ----------------------------------------------------------------------
cd ..\subsciber-printservice-feature-impl
del *.classpath
del *.project
rmdir /s /q "%cd%/target"
rmdir /s /q "%cd%/.settings"
call mvn clean install -DskipTests
if not "%ERRORLEVEL%" == "0" set /p id="Terminate batch job (Y/N)?"/b
if "%id%"=="Y" exit /b
call mvn eclipse:clean
call mvn eclipse:eclipse

ECHO ----------------------------------------------------------------------
ECHO Maven build for subsciber-printservice-feature-exec
ECHO ----------------------------------------------------------------------
cd ..\subsciber-printservice-feature-exec
del *.classpath
del *.project
rmdir /s /q "%cd%/target"
rmdir /s /q "%cd%/.settings"
call mvn clean install -DskipTests
if not "%ERRORLEVEL%" == "0" set /p id="Terminate batch job (Y/N)?"/b
if "%id%"=="Y" exit /b
call mvn eclipse:clean
call mvn eclipse:eclipse


ECHO ----------------------------------------------------------------------
ECHO Maven build for features-installer
ECHO ----------------------------------------------------------------------
cd ..\features-installer
del *.classpath
del *.project
del tm.out.*
del tmlog*
del *.epoch
rmdir /s /q "%cd%/target"
rmdir /s /q "%cd%/.settings"
call mvn clean install -DskipTests
if not "%ERRORLEVEL%" == "0" set /p id="Terminate batch job (Y/N)?"/b
if "%id%"=="Y" exit /b
call mvn eclipse:clean
call mvn eclipse:eclipse

pause