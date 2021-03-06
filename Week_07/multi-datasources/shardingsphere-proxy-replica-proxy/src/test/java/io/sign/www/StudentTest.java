package io.sign.www;

import io.sign.www.pojo.User;
import io.sign.www.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class StudentTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFind() {
        final List<User> studentList = userRepository.findAll();
        log.info("[查询所有] - [{}]", studentList);
    }

    @Test
    public void testInsert(){
        final User user = userRepository.save(User.builder().id(6).name("zxd").build());
        log.info("[添加成功] - [{}]", user);
    }
}
