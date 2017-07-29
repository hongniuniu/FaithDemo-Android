#!/bin/shexport
ProjectPath=$(cd "../$(dirname "$1")"; pwd)
export TargetClassName="ren.helloworld.wv.core.WvNative"
export SourceFile="${ProjectPath}/src/main/java"
export TargetPath="${ProjectPath}/src/main/jni"
cd "${SourceFile}"
javah -d ${TargetPath} -classpath "${SourceFile}" "${TargetClassName}"
echo -d ${TargetPath} -classpath "${SourceFile}" "${TargetClassName}"