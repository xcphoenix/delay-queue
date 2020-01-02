# Redis调用Lua脚本

## 使用脚本的好处

1. 减少网络开销
2. 原子操作
3. 可复用

## Redis + Lua

Redis 内置了 Lua 解释器，可以使用 `Eval` 命令使用脚本

1. EVAL 命令

   `EVAL script numkeys key [key...] arg [arg...]`

   - `script` : Lua 脚本程序
   - `numkeys` : 键名参数的个数
   - `key [key]` : 键名参数
   - `arg [arg...]` : 参数

   **示例**：
   ![](https://gitee.com/PhoenixXc/FigureBed/raw/picgo/img/20191231160351.png)

2. Lua 调用 Redis 命令

    Redis 支持大部分的 Lua 标准库：像 `Base`、`String`、`Table`、`Math`、`Debug` 等。
    
    在脚本中可以使用 `redis.call` 或 `redis.pcall` 来调用 Redis 命令，两者区别在于当命令出错时，`redis.pcall` 会记录错误并继续执行，`redis.call` 会直接返回错误，不会继续执行下去。
    
    在脚本中可以使用 `return` 语句将值返回客户端，如果没有 `return` 会返回 `nil`。
    
    同时两者的类型转换规则为：
    
    | Redis      | Lua     |
    | ---------- | ------- |
    | 整数        | 数字    |
    | 字符串      | 字符串  |
    | 多行字符串   | `table` |
    
    >    **Redis to Lua** conversion table.
    >
    > - Redis integer reply -> Lua number
    > - Redis bulk reply -> Lua string
    > - Redis multi bulk reply -> Lua table (may have other Redis data types nested)
    > - Redis status reply -> Lua table with a single `ok` field containing the status
    > - Redis error reply -> Lua table with a single `err` field containing the error
    > - Redis Nil bulk reply and Nil multi bulk reply -> Lua false boolean type
    >
    > **Lua to Redis** conversion table.
    >
    > - Lua number -> Redis integer reply (the number is converted into an integer)
    > - Lua string -> Redis bulk reply
    > - Lua table (array) -> Redis multi bulk reply (truncated to the first nil inside the Lua array if any)
    > - Lua table with a single `ok` field -> Redis status reply
    > - Lua table with a single `err` field -> Redis error reply
    > - Lua boolean false -> Redis Nil bulk reply.
    >
    > There is an additional Lua-to-Redis conversion rule that has no corresponding Redis to Lua conversion rule:
    >
    > - Lua boolean true -> Redis integer reply with value of 1.
    >
    > Lastly, there are three important rules to note:
    >
    > - Lua has a single numerical type, Lua numbers. There is no distinction between integers and floats. So we always convert Lua numbers into integer replies, removing the decimal part of the number if any. **If you want to return a float from Lua you should return it as a string**, exactly like Redis itself does (see for instance the [ZSCORE](https://redis.io/commands/zscore) command).
    > - There is [no simple way to have nils inside Lua arrays](http://www.lua.org/pil/19.1.html), this is a result of Lua table semantics, so when Redis converts a Lua array into Redis protocol the conversion is stopped if a nil is encountered.
    > - When a Lua table contains keys (and their values), the converted Redis reply will **not** include them.

3. EVASHA 命令

   `EVAL` 在每次执行脚本的时候都会发送一个脚本主体，虽然 Redis 有内部的缓存机制，不会每次都重新编译脚本，但是消耗带宽来传送脚本会造成浪费。

   为了减少带宽的消耗，Redis 实现了 `EVALSHA` 命令，其接受的第一个参数是脚本的 *SHA1* 摘要。

   命令的逻辑为：

   1. 如果服务器记得给定的 `SHA1` 校验和所指定的脚本，那么执行这个脚本
   2. 如果服务器不记得给定的 `SHA1` 校验和所指定的脚本，就会返回一个特殊的错误，提醒用户使用 `EVAL` 代替 `EVALSHA`

4. 获取脚本的 SHA1

    - `SCRIPT FLUSH`

      清楚所有脚本缓存

    - `SCRIPT EXISTS`

      根据给定的脚本校验，检查指定的脚本是否存在脚本缓存

    - `SCRIPT LOAD`

      将一个脚本装入脚本缓存，返回 `SHA1` 摘要，不立即运行脚本

    - `SCRIPT KILL`

      杀死当前运行的脚本




