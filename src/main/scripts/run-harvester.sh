#!/bin/bash

export LANG=en_US.UTF-8

JAVA=/lat/java/bin/java
JAR=oai-harvest-manager-${versionNumber}.jar

y=.
for x in lib/*.jar ; do
  y=${y}:${x}
done

# if LOGSUFFIX is defined, it is passed on to Java
if [ "z${LOGSUFFIX}" != "z" ]; then
  nice ${JAVA} -cp ${y} -Dlogsuffix=-${LOGSUFFIX} -jar ${JAR} $*
else
  nice ${JAVA} -cp ${y} -jar ${JAR} $*
fi
