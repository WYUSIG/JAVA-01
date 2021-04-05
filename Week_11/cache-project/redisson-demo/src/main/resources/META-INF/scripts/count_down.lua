--保存剩余库存的key
local repertory_key = KEYS[1]

--库存总数
local capacity = tonumber(ARGV[1])
local now_num = tonumber(redis.call("get", repertory_key))
--如果库存键值对不存在则初始化
if now_num == nil then
  redis.call("set", repertory_key, capacity)
  now_num = tonumber(redis.call("get", repertory_key))
end
--剩余库存大于0就减库存，并返回成功(1)和剩余库存
if now_num > 0 then
  local after_num = redis.call("decr", repertory_key)
  return { 1, after_num }
end
--否则返回失败(0)和剩余库存
return { 0, now_num }