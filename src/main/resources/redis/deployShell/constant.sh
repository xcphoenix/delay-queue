#!/bin/bash
# 定义常量
# shellcheck disable=SC2034
REDIS_NETWORK=RedisCluster
REDIS_NETWORK_IP=169.69.2.0
DOCKER_REDIS_IMAGE=redis
DOCKER_RESIS_VOLUME=/home/xuanc/docker/redis-cluster
PORT_START=7001
PORT_END=7006