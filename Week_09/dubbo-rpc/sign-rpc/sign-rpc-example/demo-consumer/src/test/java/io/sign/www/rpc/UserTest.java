package io.sign.www.rpc;

import io.sign.www.rpc.annotation.SignRpcReference;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @ClassName UserTest
 * @Description: TODO
 * @Author 钟显东
 * @Date 2021/3/18 0018
 * @Version V1.0
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class UserTest {

    @SignRpcReference
    private UserService userService;

    @Test
    public void test() {
        log.info(userService.toString());
        User user = userService.findById(1);
//        log.info(user.toString());
    }
}
