local taskDetailHash = KEYS[1];
local waitingZSET = KEYS[2];

local taskId = ARGV[1];
local taskObject = ARGV[2];
local taskExecTime = ARGV[3];

local hashKey = string.format('task:{%s}', taskId);

redis.call('HSET', taskDetailHash, hashKey, taskObject);
redis.call('ZADD', waitingZSET, taskExecTime, taskId);