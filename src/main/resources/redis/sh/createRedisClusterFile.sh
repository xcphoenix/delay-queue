#!/bin/bash
source ./constant.sh

for port in `seq ${PORT_START} ${PORT_END}`; do \
  base=6999 \
  && ip=$[port-base] \
  && mkdir -p ${DOCKER_RESIS_VOLUME}/${port}/conf \
  && PORT=${port} TEMP=${REDIS_NETWORK_IP%.0}.${ip} envsubst < redis-cluster.tmpl > ${DOCKER_RESIS_VOLUME}/${port}/conf/redis.conf \
  && mkdir -p ${DOCKER_RESIS_VOLUME}/${port}/data;\
done
