#!/bin/bash

if [ $# -ne 1 ]; then
    echo "One argument is needed"
    exit 1
fi

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

# Get Saxon
if [ ! -f ${thisDir}/saxon9he.jar ]; then
    wget -O SaxonHE9-8-0-11J.zip https://sourceforge.net/projects/saxon/files/Saxon-HE/9.8/SaxonHE9-8-0-11J.zip/download
    unzip SaxonHE9-8-0-11J.zip saxon9he.jar
    rm SaxonHE9-8-0-11J.zip
    if [ ! -f ${thisDir}/saxon9he.jar ]; then
        mv saxon9he.jar ${thisDir}/saxon9he.jar
    fi
fi
JAR=${thisDir}/saxon9he.jar

echo Command: "${JAVA} -jar ${JAR} -xsl:./resources/expandMap.xsl -it:main map=$1"
nice ${JAVA} -jar ${JAR} -xsl:${thisDir}/expandMap.xsl -it:main map=$1 2> /tmp/expand-map.log > /tmp/expand-map.csv
if [ $? -ne 0 ]; then
	echo "Failed to expand map"
    cat /tmp/expand-map.log
	exit 1
fi
mv $1 $1.bak
mv /tmp/expand-map.csv $1