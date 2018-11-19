#!/bin/bash -e


ROOT=$(dirname $0)/../..
cd $ROOT

CONFIG=local/pubber.json

DEVICE=$1
DATA=$2
PROJECT=`jq -r .projectId $CONFIG`
REGION=`jq -r .cloudRegion $CONFIG`
REGISTRY=`jq -r .registryId $CONFIG`

if [ ! -f "$DATA" ]; then
    echo Missing device or config file $DATA
    echo Usage: $0 [device] [config]
    false
fi

echo Configuring $PROJECT:$REGION:$REGISTRY:$DEVICE from $DATA

gcloud iot devices configs update --project=$PROJECT \
    --region=$REGION \
    --registry=$REGISTRY \
    --device=$DEVICE \
    --config-file=$DATA
