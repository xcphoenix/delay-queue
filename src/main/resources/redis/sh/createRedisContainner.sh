#!/bin/bash
source ./constant.sh

for port in `seq 7001 7006`; do
  base=6999;
  hostIp=$[port-base];
  echo ${hostIp};
  ip=${REDIS_NETWORK_IP%.0}.${hostIp};
  echo ${ip};

  docker run \
  -p ${port}:${port} \
  --name redis-${port} \
  -v ${DOCKER_RESIS_VOLUME}/${port}/conf/redis.conf:/usr/local/etc/redis/redis.conf \
  -v ${DOCKER_RESIS_VOLUME}/${port}/data:/data \
  --net ${REDIS_NETWORK} \
  --ip ${ip} \
  -d \
  ${DOCKER_REDIS_IMAGE};

done
