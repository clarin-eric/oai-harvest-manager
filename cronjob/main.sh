#!/usr/bin/env bash

# local test
# source $(pwd)/.oai.env && source $(pwd)/harvest.sh oai
# source $(pwd)/.nde.env && source $(pwd)/harvest.sh nde

# Clariah portainer cron
current_dir=$(cd $(dirname $0) && pwd)

if [ ! -f "${current_dir}/flag" ]; then
  touch ${current_dir}/flag
else
  echo "Previous harvest not yet complete, skip the current one."
  exit 1
fi

source ${current_dir}/.oai.env && source ${current_dir}/harvest.sh oai
source ${current_dir}/.nde.env && source ${current_dir}/harvest.sh nde

if [ ! -f "${current_dir}/flag" ]; then
  echo "Harvest completed with error: flag status not clear, please check log and files"
else
  echo "Harvest complete, clearing log"
  rm ${current_dir}/flag
fi

# ingest
source ${current_dir}/.ingest.env
if [ ${ingest:-yes} == "yes" ]; then
  echo "### Ingesting"
  source ${vlo_dir:-/data/vlo/datasets-vlo}/control.sh -s run-import
fi

echo "$(date +"%Y%m%d_%H_%M_%S") Job done, goodbye! Exiting job. "

