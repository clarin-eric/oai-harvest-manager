#!/usr/bin/env bash

# get current dir
current_dir=$(dirname $(readlink -f $0))

# ineo labeling
source "${current_dir}"/.ineo.env
echo "### Ineo labeling"
echo "### docker image [${INEO_DOCKER_IMAGE}], mapping [${INEO_MAPPING}]"
docker run -d --rm --network traefik-public --name ineo_labeling -v ${current_dir}:/app -v ${DATA_VOLUME_PRODDIR}:/srv/vlo-data -e INEO_MAPPING="${INEO_MAPPING}" -e SOLR_URL="${SOLR_URL}" -e SOLR_USER="${SOLR_USER}" -e SOLR_PASSWORD="${SOLR_PASSWORD}" "${INEO_DOCKER_IMAGE}"
