#!/bin/bash

cd /c/data/projects/common
export JAVA_HOME=`cygpath -w /c/data/installs/java`
export PATH=/c/data/installs/java/bin:$PATH
if [ ! -e build ] ; then
    mkdir build
fi
if [ ! -e dist ] ; then
    mkdir dist
fi
find src -name \*.java | xargs javac -d build -sourcepath 'src' -classpath $JAVA_HOME'\jre\lib\rt.jar;'$JAVA_HOME'\jre\lib\jce.jar' && jar cf dist/common.jar -C build com && cp dist/common.jar ../lib
