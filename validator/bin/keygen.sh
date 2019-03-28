#!/bin/bash -e

if [ -z "$1" ]; then
    echo $0 [out_dir]
    false
fi

cd $1

openssl req -x509 -nodes -newkey rsa:2048 -keyout rsa_private.pem -days 1000000 -out rsa_public.pem -subj "/CN=unused"
openssl pkcs8 -topk8 -inform PEM -outform DER -in rsa_private.pem -nocrypt > rsa_private.pkcs8
