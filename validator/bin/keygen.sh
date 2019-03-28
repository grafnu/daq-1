#!/bin/bash -e

if [ "$#" != 2 ]; then
    echo $0 [type] [out_dir] 
    false
fi

type=$1
cd $2

if [ $type == RSA_PEM ]; then
    openssl genrsa -out rsa_private.pem 2048
    openssl rsa -in rsa_private.pem -pubout -out rsa_public.pem
elif [ $type == RSA_X509_PEM ]; then
    openssl req -x509 -nodes -newkey rsa:2048 -keyout rsa_private.pem -days 1000000 -out rsa_cert.pem -subj "/CN=unused"
else
    echo Unknown key type $type. Try one of { RS256, RS256_X509 }
    false
fi

openssl pkcs8 -topk8 -inform PEM -outform DER -in rsa_private.pem -nocrypt > rsa_private.pkcs8
