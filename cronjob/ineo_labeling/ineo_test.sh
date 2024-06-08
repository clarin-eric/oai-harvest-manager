#!/usr/bin/env bash

# get current dir
current_dir=$(dirname $(readlink -f $0))

# ineo labeling
source "${current_dir}"/.ineo.env
echo "### Ineo labeling"
echo "### docker image [${INEO_DOCKER_IMAGE}], mapping [${INEO_MAPPING}]"
docker run -d --rm --network traefik-public --name ineo_labeling -v ${DATA_VOLUME_PRODDIR}:/app/proddir -v ${current_dir}/ineo_labeling.py:/app/ineo_labeling.py -e INEO_MAPPING="${INEO_MAPPING}" -e SOLR_URL="${SOLR_URL}" -e SOLR_USER="${SOLR_USER}" -e SOLR_PASSWORD="${SOLR_PASSWORD}" "${INEO_DOCKER_IMAGE}"
