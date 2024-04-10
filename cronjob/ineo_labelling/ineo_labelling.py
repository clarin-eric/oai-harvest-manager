"""
This script fetches the records from solr, and fetches the mapping file from url stored in
environment variable, and then with the help of the xml mapping file, it goes over all the
solr records and create field ineo_record. The updated solr records are stored back in solr.
The value of ineo_record is either true or false.
"""
import logging
import os
import sys
from typing import List, Optional
import ineo_labelling_utils as iu


logger = iu.get_logger(
    log_file="ineo_labelling.log",
    logger_name="ineo_labelling",
    level=logging.INFO,
    logs_folder="logs",
)


def main() -> None:
    """
    This function fetches the records from solr, and fetches the mapping file from url stored in
    environment variable, and then with the help of the xml mapping file, it goes over all the
    solr records and create field ineo_record. The updated solr records are stored back in solr.
    The value of ineo_record is either true or false.
    """
    logger.info("Starting the labelling process ...")
    logger.info("Labelling process completed.")
