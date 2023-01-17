#!/usr/bin/env bash
### usage ###
# to be used within container, not to run on host
# ./before-harvest.sh <protocol>


echo "### Preparing harvest"
# get protocol in lower case
target_protocol=${1:-oai}
target_protocol=`echo "${target_protocol}" | tr '[:upper:]' '[:lower:]'`

# prepare folder structure for OAI
echo "creating empty working folder /app/workdir/${target_protocol}"
mkdir -p /app/workdir/${target_protocol}

echo "done harvest preparation"
