#!/usr/bin/env bash

set -x

# Example
# export USERNAMR=eduyupanqui VER=v1 APP_DIR=dotnet-app && ./build.sh
# export USERNAMR=eduyupanqui VER=v1 APP_DIR=go-app && ./build.sh
# export USERNAMR=eduyupanqui VER=v1 APP_DIR=spring-app && ./build.sh
# export USERNAMR=eduyupanqui VER=v1 APP_DIR=quarkus-app && ./build.sh

# setup default values, use environment variables to override
USERNAMR="${USERNAMR:-eduyupanqui}"
VER="${VER:-latest}"
APP_DIR="${APP_DIR:-0}"
DOCKERFILE="${DOCKERFILE:-Dockerfile}"

# service-a
docker build -t ${USERNAMR}/${APP_DIR}:${VER} -f src/${APP_DIR}/${DOCKERFILE} --platform linux/amd64 src/${APP_DIR}
docker push ${USERNAMR}/${APP_DIR}:${VER}

#if has multiarch amd64 and arm64
# docker build -t ${USERNAMR}/${APP_DIR}-amd64-linux:${VER} -f src/${APP_DIR}/${DOCKERFILE} --platform linux/amd64 src/${APP_DIR}
# docker push ${USERNAMR}/${APP_DIR}-amd64-linux:${VER}

# docker manifest create ${USERNAMR}/${APP_DIR}:${VER} ${USERNAMR}/${APP_DIR}-amd64-linux:${VER}
# docker manifest push ${USERNAMR}/${APP_DIR}:${VER}
