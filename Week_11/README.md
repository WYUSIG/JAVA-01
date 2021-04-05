作业总结(详细效果看cache-project的单元测试)

##### 在 Java 中实现一个简单的分布式锁；

我使用了Redisson来实现

核心代码：

```java
RLock lock = redissonClient.getLock(LOCK_KEY);
boolean res = lock.tryLock(100, 10, TimeUnit.SECONDS);
lock.unlock();
```

##### 在 Java 中实现一个分布式计数器，模拟减库存。

我自己编写了一个lua脚本，通过Spring-Boot创建一个RedisScript的Bean，使用redisTemplate#execute，把RedisScript、keys、args传进去即可

lua脚本内容

```java
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
```

##### 基于 Redis 的 PubSub 实现订单异步处理

Subscribe核心代码

```java
@Bean
public RedisMessageListenerContainer listenerContainer(RedisConnectionFactory factory) {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(factory);
    container.addMessageListener(new TestChannelTopicMessageListener(), new ChannelTopic("TEST"));
    return container;
}
public class TestChannelTopicMessageListener implements MessageListener {
    @Override
    public void onMessage(Message message, byte[] pattern) {
        System.out.println("收到消息：");
        System.out.println("线程编号：" + Thread.currentThread().getName());
        System.out.println("message：" + message);
        System.out.println("pattern：" + new String(pattern));
    }

}
```

Publisher核心代码

```java
stringRedisTemplate.convertAndSend(TOPIC, "模拟消息");
```

