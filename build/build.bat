@echo off
set dir_name=dynviz-0.0.3-release

cd ..

if "%~2" == "" (
  echo Usage: build output_directory profile [profile2 ...]
  echo where profile can be 'win32', 'win64', 'linux32', 'linux64' or 'macosx'
  goto:end
)

set out=%1

:loop
shift
if "%~1" NEQ "" (
  set profile=%1
  call:build
  goto:loop
)

goto:end

:build
echo Building profile: %profile%
call mvn clean install -P%profile%
rd /S /Q "%out%/%dir_name%-%profile%"
xcopy /E /I /Q "build/target/%dir_name%" "%out%/%dir_name%-%profile%"
goto:eof

:end