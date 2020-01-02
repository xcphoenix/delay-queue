# 添加队列


使用 Lua 脚本，实现原子操作。

1. 添加到任务【任务信息队列】

   - `keys`
     - hash ： `{TOPIC}:DETAIL`

   - `args`
     - key  ： `task:{id}`
     - value： `Json.toJsonString(taskObject)`

2. 添加任务到【待消费队列】

   - `keys`
     - ZSET：`{TOPIC}:WAITING`

   - `args`
     - taskId
     - taskExecTime

