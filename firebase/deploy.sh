#!/bin/bash -e

ROOT=$(realpath $(dirname $0)/..)
cd $ROOT

if [ $# != 1 ]; then
    echo Usage: $0 [project_id]
    false
fi

PROJECT=$1

CFILE=firebase_config.js

echo
echo For local hosting: firebase serve --only hosting --project $PROJECT
echo Subscription pull: gcloud pubsub subscriptions pull --auto-ack daq_monitor --project $PROJECT
echo Firestore address: https://console.cloud.google.com/firestore/data/?project=$PROJECT
echo Application host : https://$PROJECT.firebaseapp.com
echo

if [ -f firebase/public/$CFILE ]; then
    echo Using existing firebase/public/$CFILE
elif [ -f local/$CFILE ]; then
    echo Copying local/$CFILE to firebase/public/
    cp local/$CFILE firebase/public/
else
    echo No local/$CFILE found.
    false
fi

cd firebase

echo firebase deploy --project $PROJECT
firebase deploy --project $PROJECT
