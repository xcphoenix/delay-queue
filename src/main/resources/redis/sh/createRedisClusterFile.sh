#!/bin/bash
source ./constant.sh 

for port in `seq 7001 7006`; do \
  base=6999 \
  && ip=$[port-base] \
  && mkdir -p ${CWD}/${port}/conf \
  && PORT=${port} TEMP=${REDIS_NETWORK_IP%.0}.${ip} envsubst < redis-cluster.tmpl > ${CWD}/${port}/conf/redis.conf \
  && mkdir -p ${CWD}/${port}/data;\
done
