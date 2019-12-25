#!/bin/bash
source ./constant.sh 
docker network create --subnet=${REDIS_NETWORK_IP}/24 ${REDIS_NETWORK}
