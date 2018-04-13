#!/bin/bash

READLINK="readlink"

if [ $# -ne 2 ]; then
    echo "Two arguments are needed"
    exit 1
fi

thisDir="$(dirname "$(${READLINK} -f "$0")")"

if [ "z${BASE_DIR}" == "z" ]; then
  BASE_DIR=$thisDir
fi
if [ "z${BIN_DIR}" == "z" ]; then
  BIN_DIR=$thisDir
fi

source $thisDir/oaiharvest-env.sh

WORK_DIR=$BASE_DIR/work/$2
TMP_DIR=$BASE_DIR/tmp/zzz-$2
LOG_DIR=$WORK_DIR/logs
LOGS_DIR=$BASE_DIR/log
OUTPUT_DIR=$BASE_DIR/$2
RESULTSET_DIR=$BASE_DIR/resultsets

echo "WORK_DIR=${WORK_DIR}"
echo "TMP_DIR=${TMP_DIR}"
echo "LOG_DIR=${LOG_DIR}"
echo "LOGS_DIR=${LOGS_DIR}"
echo "OUTPUT_DIR=${OUTPUT_DIR}"
echo "RESULTSET_DIR=${RESULTSET_DIR}"

export LOG_DIR
export PROPS

if [ ! -d $BASE_DIR ]; then
	echo "$BASE_DIR does not exist; aborting!"
	exit 1
fi

if [ ! -d $TMP_DIR ]; then
        echo "Temp dir [$TMP_DIR] does not exist; creating now"
        mkdir -p "$TMP_DIR"
fi

if [ ! -d $OUTPUT_DIR ]; then
        echo "Output dir [$OUTPUT_DIR] does not exist; creating now"
        mkdir -p "$OUTPUT_DIR"
fi

if [ ! -d $RESULTSET_DIR ]; then
	echo "Resultset dir [$RESULTSET_DIR] does not exist; creating now"
	mkdir -p "$RESULTSET_DIR/backups"
fi

if [ ! -d $LOG_DIR ]; then
	echo "Log dir [$LOG_DIR] does not exist; creating now"
	mkdir -p "$LOG_DIR"
fi

if [ ! -d $LOGS_DIR ]; then
	echo "Logs dir [$LOGS_DIR] does not exist; creating now"
	mkdir -p "$LOGS_DIR"
fi

#
# Prepare 
#
cd $BIN_DIR
if [ -d $WORK_DIR ]; then
	rm -rf $WORK_DIR
fi
mkdir -p $WORK_DIR

#
# Start harvesting into $WORK_DIR
#

echo Command: "`pwd`/./run-harvester.sh workdir=$WORK_DIR map-file=$WORK_DIR/map.csv $1"
./run-harvester.sh workdir=$WORK_DIR map-file=$WORK_DIR/map.csv $1

if [ $? -ne 0 ]; then
	echo "Failed to run harvester"
	exit 1
fi

#
# Expand map.csv
#

echo Command: "`pwd`/./expand-map.sh $WORK_DIR/map.csv"
./expand-map.sh $WORK_DIR/map.csv

#
# Archive log files
#
if [ -d "$LOG_DIR" ]; then
	cd $LOG_DIR
	nice tar cjf $LOGS_DIR/oai-harvester-$2.log.`date "+%Y-%m-%d"`.tar.bz2 *
	cd $thisDir
fi

#
# Copy old production directory to backup location
#
echo "Creating backup"
nice rm -rf $TMP_DIR
mkdir $TMP_DIR
nice mv $OUTPUT_DIR/* $TMP_DIR

#
# Copy harvest output from work to production location
#
if [ "$2" != "meertens" ]; then
	echo "Moving to output directory"
	nice mv $WORK_DIR/* $OUTPUT_DIR/
fi

#
# For the clarin run, the meertens/vu data which is harvested seperately, is copied in
#
if [ "$2" == "clarin" ]; then
#	if [ -d $BASE_DIR/work/meertens ]; then
# for now Meertens is back again in the main CLARIN harvest
#        nice rsync -av  $BASE_DIR/work/meertens/ $OUTPUT_DIR
#	fi
	if [ -d $BASE_DIR/work/vu ]; then
        nice rsync -av  $BASE_DIR/work/vu/ $OUTPUT_DIR
	fi
fi

#
# Create bz2 archive, except for the meertens run
#
if [ "$2" != "meertens" ]; then
	echo "Archiving"
	cd $OUTPUT_DIR
	nice tar cfj $2.tar.bz2 results
	targ=$RESULTSET_DIR/$2.tar.bz2
	if [ -f $targ ]; then
 		nice mv -f $targ $RESULTSET_DIR/backups/$2.`date "+%Y-%m-%d"`.tar.bz2
	fi
	nice mv $2.tar.bz2 $targ
	nice rm -rf $TMP_DIR
fi

#
# Create web-view
#
if [ -f /opt/oai-webview.pl ]; then
	nice perl -CSD /opt/oai-webview.pl
fi

exit 0
