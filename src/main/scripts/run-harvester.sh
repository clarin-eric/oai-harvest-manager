#!/bin/bash

JAVA="java"
READLINK="readlink"

function ensureSlash(){
  length=${1}-1

  # If the parameter passed to the function does not end with a slash, append
  # one and return the result
  if [ "{$1:length}" != "/" ]; then
    echo ${1}/
  fi
}

export LANG=en_US.UTF-8

# Do not assume the script is invoked from the directory it is located in; get
# the directory the script is located in
thisDir="$(dirname "$(${READLINK} -f "$0")")"
JAR=$thisDir/oai-harvest-manager-${versionNumber}.jar

# Determine the logging mode
if [ "z${LOGDIR}" != "z" ]; then
  LOGDIR=$(ensureSlash $LOGDIR)

  PROPS="${PROPS} -Dlogdir=${LOGDIR}"
fi

if [ "z${LOGPROPS}" != "z" ]; then
  PROPS="${PROPS} -Dlog4j.configuration=file://${LOGPROPS}"
else
  PROPS="${PROPS} -Dlog4j.configuration=file://${PWD}/resources/log4j.properties"
fi

if [ "z${LOGSUFFIX}" != "z" ]; then
  PROPS="${PROPS} -Dlogsuffix=-${LOGSUFFIX}"
fi

nice ${JAVA} ${PROPS} -jar ${JAR} $*
