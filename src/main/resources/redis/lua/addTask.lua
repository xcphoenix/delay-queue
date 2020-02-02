-- 任务Hash
local taskDetailHash = KEYS[1];
-- 等待队列
local waitingZSET = KEYS[2];

-- 任务id
local taskId = ARGV[1];
-- 任务序列化对象
local taskObject = ARGV[2];
-- 任务执行时间
local taskExecTime = ARGV[3];

-- 设置key
local hashKey = string.format('task:{%s}', taskId);

-- 放入任务Hash
redis.call('HSET', taskDetailHash, hashKey, taskObject);
-- 添加到等待队列
redis.call('ZADD', waitingZSET, taskExecTime, taskId);

return nil