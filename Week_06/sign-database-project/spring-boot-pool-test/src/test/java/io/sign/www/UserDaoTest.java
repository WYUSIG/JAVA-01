package io.sign.www;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.sign.www.repository.UserDao;
import io.sign.www.pojo.User;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class UserDaoTest {

    @Autowired
    private UserDao userDao;

    @Test
    public void test1(){
//        final User user = userDao.save(User.builder().id(1).userName("李焕英").build());
//        log.info("[添加成功] - [{}]", user);
        final List<User> userList = userDao.findAll();
        log.info("[查询所有] - [{}]", userList);
    }


}
