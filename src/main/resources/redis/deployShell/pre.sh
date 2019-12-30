#!/bin/bash
source ./constant.deployShell

# create network
docker network create --subnet=${REDIS_NETWORK_IP}/24 ${REDIS_NETWORK}

# create rerdis-cluster
# master:slave 1:1
redis-cli --cluster create 169.69.2.2:7001 169.69.2.3:7002 169.69.2.4:7003 169.69.2.5:7004 169.69.2.6:7005 169.69.2.7:7006 --cluster-replicas 1
