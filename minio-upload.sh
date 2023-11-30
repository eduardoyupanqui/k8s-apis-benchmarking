#!/usr/bin/env bash

set -x
# chmod +x minio-upload.sh

# Usage: ./minio-upload images ./minio/images/thumbnail.png

bucket=$1
path=$2

host='localhost:9000'
s3_key=admin
s3_secret=devops123

file="$(basename ${path})"
resource="/${bucket}/${file}"
content_type="application/octet-stream"
date=`date -R`
_signature="PUT\n\n${content_type}\n${date}\n${resource}"
signature=`echo -en ${_signature} | openssl sha1 -hmac ${s3_secret} -binary | base64`

curl -X PUT -T "${path}" \
          -H "Host: ${host}" \
          -H "Date: ${date}" \
          -H "Content-Type: ${content_type}" \
          -H "Authorization: AWS ${s3_key}:${signature}" \
          http://${host}${resource}