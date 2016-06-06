#!/bin/bash

UNITYEDITOR_DEFAULT=/Applications/Unity/Unity.app/Contents/MacOS/Unity
UNITYEDITOR="${UNITYEDITOR:-$UNITYEDITOR_DEFAULT}"

$UNITYEDITOR -force-free -nographics -quit -batchmode -projectPath $PWD/ThirdpresenceAdSDK -exportPackage Assets/Plugins $PWD/export/thirdpresence-ad-sdk.unitypackage
