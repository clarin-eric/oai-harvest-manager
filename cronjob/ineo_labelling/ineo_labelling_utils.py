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


def get_logger(log_file: str, logger_name: str,
               level: int = logging.DEBUG, logs_folder: str = "logs") -> logging.Logger:
    logger = logging.getLogger(logger_name)
    logger.setLevel(level)

    # Ensure the "logs" folder exists
    if not os.path.exists(logs_folder):
        os.makedirs(logs_folder)

    log_format = "%(asctime)s - %(levelname)s - %(message)s"
    formatter = logging.Formatter(log_format)

    log_file = os.path.join(logs_folder, log_file)
    file_handler = logging.FileHandler(log_file)
    file_handler.setFormatter(formatter)
    logger.addHandler(file_handler)

    # Add a stdout handler
    stdout_handler = logging.StreamHandler(sys.stdout)
    stdout_handler.setFormatter(formatter)
    logger.addHandler(stdout_handler)
    return logger

