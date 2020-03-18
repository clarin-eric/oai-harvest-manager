#!/bin/bash 
JAVA_TARGET_DIR="$(cd "$(dirname $0)" && pwd)/target"
JAVA_IMAGE=registry.gitlab.com/clarin-eric/docker-alpine-supervisor-java-base:openjdk8-1.2.5
CONTAINER_CONF_FILE_PATH='/tmp/harvester.conf'
JAVA_CMD="java -jar /java-bin/oai-harvest-manager*.jar ${CONTAINER_CONF_FILE_PATH}"
CONFIG_FILE="$1"

if ! [ "${CONFIG_FILE}" ]; then
	echo "Usage: $0 <config file>" >&2
	exit 1
fi

if ! [ -e "${CONFIG_FILE}" ]; then
	echo "File does not exist: $0" >&2
	exit 1
fi

if ! [ -d "${JAVA_TARGET_DIR}" ]; then
	echo "Target dir ${JAVA_TARGET_DIR} does not exist. Run build.sh first?" >&2
	exit 1
fi

docker run -it --rm \
	--name "oai-harvest-test-${RANDOM}" \
	--volume "${JAVA_TARGET_DIR}:/java-bin" \
	--volume "$(realpath "$CONFIG_FILE"):${CONTAINER_CONF_FILE_PATH}" \
	--volume "$(pwd)/run/workdir:/workdir" \
	--entrypoint bash \
	"${JAVA_IMAGE}" -c "cd /workdir && ${JAVA_CMD}"
