"""
This script fetches the records from solr, and fetches the mapping file from url stored in
environment variable, and then with the help of the xml mapping file, it goes over all the
solr records and create field ineo_record. The updated solr records are stored back in solr.
The value of ineo_record is either true or false.
"""
import os
import logging
from functools import cache

import requests
import concurrent.futures
import ineo_labeling_utils as iu
from typing import List, Dict
from xml.etree import ElementTree as ET
from ineo_classes import Providers, Provider
from pyfat.__main__ import evaluate

# get environment variable INEO_MAPPING
INEO_MAPPING = os.getenv("INEO_MAPPING")
SOLR_URL = os.getenv("SOLR_URL")
SOLR_USER = os.getenv("SOLR_USER")
SOLR_PASSWORD = os.getenv("SOLR_PASSWORD")

logger = iu.get_logger(
    log_file="ineo_labeling.log",
    logger_name="ineo_labeling",
    level=logging.INFO,
    logs_folder="logs",
)


@cache
def fetch_xml_from_url(url: str) -> ET.Element:
    response = requests.get(url)
    response.raise_for_status()  # Raise exception if the request failed
    xml_content = response.content
    logger.debug(f"XML content fetched from {url}, content: {xml_content}")
    etree = ET.ElementTree(ET.fromstring(xml_content))
    return etree.getroot()


def fetch_mapping_file(mapping_url: str) -> ET.Element:
    """
    This function fetches the mapping file from the url and stores it locally.
    """
    logger.info(f"Fetching the mapping file from {mapping_url}")
    # Fetch the mapping file from the url and store it locally
    return fetch_xml_from_url(mapping_url)


def _add_provider(providers: Providers, provider: ET.Element) -> None:
    provider_name = provider.get("name", None)
    logger.info(f"{provider_name=}")

    provider_node = provider.find("assessment")
    if provider_node is None:
        provider_assessment = False
    else:
        provider_assessment = provider_node.text
        if provider_assessment == "true":
            provider_assessment = True
        else:
            provider_assessment = False

    profile_node = provider.find("profile")
    if profile_node is None:
        provider_profile = None
    else:
        provider_profile = profile_node.text

    level_node = provider.find("level")
    if level_node is None:
        provider_level = None
    else:
        provider_level = int(level_node.text)

    default_node = provider.find("default")
    if default_node is None:
        provider_default = None
    else:
        provider_default = default_node.text
        if provider_default == "true":
            provider_default = True
        else:
            provider_default = False

    provider_obj = Provider(
        name=provider_name,
        profile=provider_profile,
        level=provider_level,
        assessment=provider_assessment,
        default=provider_default
    )
    providers.providers.append(provider_obj)
    providers.provider_keys.append(provider_name)


def parse_mapping_file(mapping_tree: ET.Element) -> Providers:
    """
    Parses the mapping file using the ineo_classes and returns a providers object with the mapping.

    The mapping file is xml and has the following structure:
    b'<?xml version="1.0" encoding="UTF-8"?>\n<datasets default="false">\n    <provider name="Meertens Institute - Research Collections">\n        <profile>clarin.eu:cr1:p_1440426460262</profile>\n        <!-- <profile/> -->\n    </provider>\n    <provider name="The Language Archive">\n        <level>0</level>\n        <!-- <level/> -->\n    </provider>\n    <provider name="DANS">\n        <default>true</default>\n    </provider>\n    <root name="CLARIN Partners">\n        <default>true</default>\n        <!-- \n            <level/>\n            <profile>\n        -->\n    </root>\n    <root name="NDE Partners">\n        <default>true</default>\n    </root>\n</datasets>\n'
    """
    providers = Providers()
    providers.default = mapping_tree.get("default", None)
    if providers.default == "true":
        providers.default = True
    else:
        providers.default = False

    for provider in mapping_tree.findall("provider") or mapping_tree.findall("root"):
        provider_name = provider.get("name", None)

        profile_node = provider.find("profile")
        if profile_node is None:
            provider_profile = None
        else:
            provider_profile = profile_node.text

    for provider in mapping_tree.findall("provider"):
        _add_provider(providers, provider)

    for provider in mapping_tree.findall("root"):
        _add_provider(providers, provider)
    return providers


def _fetch_solr_records(query: str, solr_url: str, username, password, start=0, rows=10000) -> Dict:
    """
    Retrieve Solr records in parallel with a given query.
    """
    params = {
        "q": query,
        "wt": "json",
        "start": start,
        "rows": rows,
    }
    response = requests.get(f"{solr_url}/select", params=params, auth=(username, password))
    response.raise_for_status()  # Raise exception if the request failed
    data = response.json()
    return data["response"]


def fetch_solr_records(query: str, solr_url: str, username, password) -> List[Dict]:
    """
    Retrieve Solr records in parallel with a given query.
    """
    # Retrieve the total number of records
    response = _fetch_solr_records(query, solr_url, username, password, start=0, rows=0)
    total_records = response["numFound"]
    logger.info(f"Total records in Solr: {total_records}")

    # Retrieve the records in parallel
    records = []
    with concurrent.futures.ThreadPoolExecutor() as executor:
        futures = []
        for start in range(0, total_records, 10000):
            futures.append(
                executor.submit(
                    _fetch_solr_records, query, solr_url, username, password, start=start, rows=10000
                )
            )
        for future in concurrent.futures.as_completed(futures):
            records.extend(future.result()["docs"])
    return records


def update_solr_records(doc: Dict, solr_url: str, username, password) -> None:
    """
    Update a single Solr record with specific field.
    """
    # Prepare the update payload
    payload = {"add": {"doc": doc, "commitWithin": 1000}}
    # Send the update request
    response = requests.post(f"{solr_url}/update", json=payload, auth=(username, password))
    response.raise_for_status()  # Raise exception if the request failed
    logger.info(f"Record updated: {doc['id']}")


def _check_provider_or_root(doc: Dict, mapping: Providers, provider_name: str | None) -> bool:
    """
    Check if the given provider or root is an INEO record.

    :param doc:
    :param mapping:
    :param provider_name:
    :return:
    """
    provider_profile = doc.get("_componentProfileId", None)
    provider_level = doc.get("_hierarchyWeight", None)

    logger.info(f"provider_profile: {provider_profile=}, {mapping.get_provider(provider_name).profile=}")
    logger.info(f"provider_level: {provider_level=}, {mapping.get_provider(provider_name).level=}")
    logger.info(f"default: {mapping.get_provider(provider_name).default}")
    if provider_profile is not None and provider_profile == mapping.get_provider(
            provider_name).profile:
        logger.info(
            f"### {provider_name} ### result: {provider_profile == mapping.get_provider(provider_name).profile}")
        return True
    if provider_level is not None and provider_level == mapping.get_provider(
            provider_name).level:
        logger.info(
            f"type of provider_level: {type(provider_level)}, {type(mapping.get_provider(provider_name).level)}")
        logger.info(f"result: {provider_level == mapping.get_provider(provider_name).level}")
        return True
    if mapping.get_provider(provider_name).default is not None:
        logger.info(f"provider default: {mapping.get_provider(provider_name).default}")
        return mapping.get_provider(provider_name).default
    return False


def _is_ineo_record(doc: Dict, mapping: Providers) -> tuple[bool, bool]:
    """
    Check if the Solr record is an INEO record and needs FAIR assessment

    :param doc: Solr record
    :param mapping: Providers object
    :return: bool, bool The first value in the tuple indicates if the record is an INEO record,
                and the second value shows if the record needs assessment.
    """
    result_provider_name: bool = False
    result_root_name: bool = False
    provider_name = doc.get("dataProvider", None)
    root_name = doc.get("_harvesterRoot", None)
    do_assessment: bool = False

    if provider_name is None:
        raise ValueError("Provider name is missing in the Solr record.")

    logger.info(f"{provider_name=}")
    if provider_name in mapping.provider_keys:
        result_provider_name = _check_provider_or_root(doc, mapping, provider_name)
        do_assessment = mapping.get_provider(provider_name).assessment
    elif root_name in mapping.provider_keys:
        result_root_name = _check_provider_or_root(doc, mapping, root_name)
        do_assessment = mapping.get_provider(root_name).assessment

    if result_provider_name or result_root_name:
        return True, do_assessment
    else:
        logger.info(f"global default: {mapping.default}")
        return mapping.default, do_assessment


def _label_ineo_records(docs: List[Dict], mapping: Providers) -> List[Dict]:
    """
    Label the Solr records with the ineo_record field.
    """
    ttl_path: str = "/srv/vlo-data/ttl"
    good = 0
    bad = 0
    payload = []
    for doc in docs:
        logger.info(f"Processing doc: {doc['id']}")
        ineo_record, do_assessment = _is_ineo_record(doc, mapping)
        # TODO FIXME: re-think the logic of caching and remove the static 'caching' below and in the bottom of this function
        # TODO NOTE:
        """
        TODO: do_assessment is determined, the line below should become a function call to determine the validity and expire of the cache.
        The check is only needed when do_assessment is True.
        if do_assessment:
            do_assessement = check_cache(doc['id'], ttl_path)
        """
        if os.path.isfile(os.path.join(ttl_path, f"{doc['id']}.ttl")):
            do_assessment = False

        env_result = None
        if ineo_record:
            logger.debug(f"INEO record: {doc['id']}: {ineo_record}")
            cmdi_file = doc.get("_fileName", None)
            if cmdi_file is None:
                logger.error(f"CMDI file is missing in the Solr record: {doc['id']}")
                exit(1)
            if do_assessment:
                logger.info(f"Assessment needed: {doc['id']}: {do_assessment}")
                variable_dict = {"FACETS": doc}
                env_result = evaluate(cmdi_file, variable_dict)
                assessment_score = env_result.score
            else:
                logger.info(f"No assessment needed: {doc['id']}: {do_assessment}")
                assessment_score = -1
            good += 1
        else:
            bad += 1
            assessment_score = -1
        # Add check result and assessment result to payload, to be written back to solr
        print(f"Assessment score: {assessment_score}")
        if not do_assessment or env_result is None:
            payload.append({
                "id": doc["id"],
                "ineo_record": {"set": ineo_record}
            })
        else:
            payload.append({
                "id": doc["id"],
                "ineo_record": {"set": ineo_record},
                "fair_score": {"set": assessment_score}
            })
            # Write the assessment result to a file
            try:
                with open(os.path.join(ttl_path, f"{doc['id']}.ttl"), "w") as f:
                    f.write(repr(env_result))
            except OSError as e:
                print(f"Error writing to file: {e}")
    logger.debug(f"{bad=} and {good=}")
    return payload


def label_ineo_records(query: str, solr_url: str, mapping: Providers) -> None:
    """
    Retrieve and update Solr records in parallel.
    """
    batch_size = 1000

    # Retrieve the total number of records
    docs = fetch_solr_records(query, solr_url, SOLR_USER, SOLR_PASSWORD)
    total_records = len(docs)
    logger.info(f"### Total records in Solr: {total_records}")

    for i in range(0, total_records, batch_size):
        logger.info(f"\n\n##### Processing batch {i} to {i+batch_size}")
        # Retrieve a batch of records
        batch = docs[i:i+batch_size]
        update_docs = _label_ineo_records(batch, mapping)

        # Update Solr records in parallel
        with concurrent.futures.ThreadPoolExecutor(max_workers=12) as executor:
            args = [(doc, solr_url, SOLR_USER, SOLR_PASSWORD) for doc in update_docs]
            executor.map(lambda p: update_solr_records(*p), args)


def main() -> None:
    """
    This function fetches the records from solr, and fetches the mapping file from url stored in
    environment variable, and then with the help of the xml mapping file, it goes over all the
    solr records and create field ineo_record. The updated solr records are stored back in solr.
    The value of ineo_record is either true or false.
    """
    logger.info("Starting the labelling process ...")
    logger.debug(f"{SOLR_URL=}")

    """
    Parse the mapping file and create a providers object.
    """
    mapping_tree = fetch_mapping_file(INEO_MAPPING)
    providers: Providers = parse_mapping_file(mapping_tree)
    logger.info(f"providers: {providers}")

    """
    label ineo records
    """
    label_ineo_records("*:*", SOLR_URL, providers)

    logger.info("Labelling process completed.")


if __name__ == "__main__":
    main()
