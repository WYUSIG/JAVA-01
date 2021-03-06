package io.sign.www;

import io.sign.www.entity.User;
import io.sign.www.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    public void testSave() {
        User user = User.builder().id(4).name("zzz").build();
        userService.save(user);
    }

    @Test
    public void testFind() {
        User user = userService.getById(1);
        log.info(user.toString());
    }
}
