#!/bin/bash

if [ ! -e build ] ; then
    mkdir build
fi
if [ ! -e dist ] ; then
    mkdir dist
fi
find src -name \*.java | xargs jikes +D -d build -sourcepath 'src' -classpath $JAVA_HOME'\jre\lib\rt.jar;'$JAVA_HOME'\jre\lib\jce.jar' && jar cf dist/common.jar -C build com && cp dist/common.jar ../lib