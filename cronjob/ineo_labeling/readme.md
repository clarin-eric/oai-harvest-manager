# Label Ineo Records

This repository contains the code to label the Ineo dataset. The records are in the form of JSON objects retrieved
from Solr. The goal is to label the records as `ineo_record: true` when they should be picked up by the INEO ingest 
pipeline. The rest of the records should be labeled as `ineo_record: false`.

## Usage
 * Build container ```./build.sh```
 * The container can be run with the following command:
    ```bash
    docker run -v /path/to/ineo_labeling:/ineo_labeling -v /path/to/output:/output -it ineo_labeling
    ```
   though it will be automatically invoked by cron job

## Configuration
there is the `.ineo.env` file that contains the following variables:
```bash
SOLR_URL=http://solr:8983/solr/ineo
.
```
This file contains the needed configuration for the script to run.

