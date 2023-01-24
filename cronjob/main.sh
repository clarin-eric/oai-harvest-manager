#!/usr/bin/env sh

# local test
# source $(pwd)/.oai.env && source $(pwd)/harvest.sh oai
# source $(pwd)/.nde.env && source $(pwd)/harvest.sh nde

# Clariah portainer cron
source $(current_dir)/.oai.env && source $(current_dir)/harvest.sh oai
source $(current_dir)/.nde.env && source $(current_dir)/harvest.sh nde


