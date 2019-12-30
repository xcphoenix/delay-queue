#!/bin/bash

source ./constant.deployShell

regex=redis-700
docker container ls -a | grep ${regex} | awk '{print $1}' | xargs docker container rm -f
rm -ri ${DOCKER_RESIS_VOLUME}/*
