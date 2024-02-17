#!/usr/bin/env bash

current_dir=$(cd $(dirname $0) && pwd)
# make sure require vars are set
if [ -z ${DATA_VOLUME} ] || [ -z ${HARVESTER_CONFIG} ] || [ -z ${DOCKER_IMAGE} ]; then
  echo "missing vars";
  exit 1;
fi

echo "### $(date +"%Y%m%d_%H_%M_%S") Harvesting using harvester [${DOCKER_IMAGE}] and config is [${HARVESTER_CONFIG}]"

# get protocol in lower case
target_protocol=${1:-oai}
target_protocol=`echo "${target_protocol}" | tr '[:upper:]' '[:lower:]'`

# before harvest, prepare folder structure
docker run --rm -v ${DATA_VOLUME}:/app/workdir -v ${current_dir}/before-harvest.sh:/tmp/before-harvest.sh busybox:latest sh /tmp/before-harvest.sh ${target_protocol}


### run harvest
if [ "${harvest:-yes}" = "yes" ]; then
  docker run --rm -e LOG_DIR=/app/workdir -v ${DATA_VOLUME}:/app/workdir ${DOCKER_IMAGE} workdir=/app/workdir/${target_protocol} ${HARVESTER_CONFIG}
fi

# after harvest, prepare proddir
docker run --rm -v ${DATA_VOLUME}:/app/workdir -v ${DATA_VOLUME_PRODDIR}:/app/proddir -v ${current_dir}/after-harvest.sh:/tmp/after-harvest.sh busybox:latest sh /tmp/after-harvest.sh ${target_protocol}
