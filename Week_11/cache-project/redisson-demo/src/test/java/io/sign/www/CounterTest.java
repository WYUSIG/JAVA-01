package io.sign.www;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CounterTest {

    @Autowired
    private RedisTemplate redisTemplate;

    private static final String KEY = "counter";

    @Autowired
    private RedisScript<List<Long>> script;

    /**
     * 模拟100库存的减库存
     */
    @Test
    public void test() {
        List<String> keys = new ArrayList<>();
        keys.add(KEY);
        List<Long> res = (List<Long>) redisTemplate.execute(this.script, keys, 100);
        if(res.get(0).equals(1L)) {
            System.out.println("抢单成功，剩余库存："+res.get(1));
        } else {
            System.out.println("抢单失败，剩余库存："+res.get(1));
        }
    }
}
