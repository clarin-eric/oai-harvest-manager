#!/usr/bin/env bash

# local test
# source $(pwd)/.oai.env && source $(pwd)/harvest.sh oai
# source $(pwd)/.nde.env && source $(pwd)/harvest.sh nde

# Clariah portainer cron
current_dir=$(cd $(dirname $0) && pwd)

if [ ! -f "${current_dir}/flag" ]; then
  touch "${current_dir}"/flag
else
  echo "Previous harvest not yet complete, skip the current one."
  exit 1
fi

source "${current_dir}"/.nde.env && source "${current_dir}"/harvest.sh nde
source "${current_dir}"/.oai.env && source "${current_dir}"/harvest.sh oai

# ingest
source "${current_dir}"/.ingest.env
if [ "${ingest:-yes}" == "yes" ] && [ "${can_ingest:-yes}" == "yes" ]; then
  echo "### Ingesting"
  cd "${vlo_dir:-/data/vlo/datasets-vlo}" && ./control.sh -s run-import
else
  info "Ingest skipped due to configuration or unclear harvest flag"
fi

# ineo labeling
source "${current_dir}"/ineo_labeling/.ineo.env
echo "### docker image [${INEO_DOCKER_IMAGE}], mapping [${INEO_MAPPING}]"
docker run -d --rm --network traefik-public --name ineo_labeling -e INEO_MAPPING="${INEO_MAPPING}" -e SOLR_URL="${SOLR_URL}" -e SOLR_USER="${SOLR_USER}" -e SOLR_PASSWORD="${SOLR_PASSWORD}" "${INEO_DOCKER_IMAGE}"

# cleanup
if [ ! -f "${current_dir}/flag" ]; then
  echo "Harvest completed with error: flag status not clear, please check log and files"
else
  echo "Harvest complete, clearing log"
  rm "${current_dir}"/flag
  can_ingest="yes"
fi

echo "$(date +"%Y%m%d_%H_%M_%S") Job done, goodbye! Exiting job. "
