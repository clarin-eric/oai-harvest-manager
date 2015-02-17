#!/bin/bash

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
thisDir="$(dirname "$(readlink -f "$0")")"
JAR=$thisDir/oai-harvest-manager-${versionNumber}.jar

# Determine the logging mode
if [ "z${HLOGDIR}" == "z" ]; then

  # the HLOGDIR environment variable has not been defined, use the log4j
  # properties file supplied in the package

  if [ "z${LOGSUFFIX}" != "z" ]; then
    nice java -Dlog4j.configuration=resources/log4j.properties -Dlogsuffix=-${LOGSUFFIX} -jar ${JAR} $*
  else
    nice java -Dlog4j.configuration=resources/log4j.properties -jar ${JAR} $*
  fi
else
  # the HLOGDIR environment variable has been defined; make sure HLOGDIR ends
  # with /

  HLOGDIR=$(ensureSlash $HLOGDIR)

  # Pass HLOGDIR to java as the logdir property, the log4j framework will pick
  # it up as such; please refer to the log4j.properties. If defined, also pass
  # on the LOGSUFFIX to the framework.

  if [ "z${LOGSUFFIX}" != "z" ]; then
    nice java -Dlogdir=$HLOGDIR -Dlogsuffix=-${LOGSUFFIX} -jar ${JAR} $*
  else
    nice java -Dlogdir=$HLOGDIR -jar ${JAR} $*
  fi
fi
