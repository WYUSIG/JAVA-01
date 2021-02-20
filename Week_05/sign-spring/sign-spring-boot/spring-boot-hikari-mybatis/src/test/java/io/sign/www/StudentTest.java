package io.sign.www;

import io.sign.www.dao.StudentDao;
import io.sign.www.pojo.Student;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class StudentTest {

    @Autowired
    private StudentDao studentDao;

    @Test
    public void test1() {
        int count1 = studentDao.insert(new Student(102, "王宝强"));
        log.info("[添加成功] - [{}]", count1);
        Student student = studentDao.findById(102);
        log.info("[查询所有] - [{}]", student);
        int count2 = studentDao.updateById(new Student(102, "贾玲"));
        log.info("[修改成功] - [{}]", count2);
    }
}
