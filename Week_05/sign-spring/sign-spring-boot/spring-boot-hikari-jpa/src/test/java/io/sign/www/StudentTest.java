package io.sign.www;

import io.sign.www.pojo.Student;
import io.sign.www.repository.StudentRepository;
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
    private StudentRepository studentRepository;

    @Test
    public void test1() {
        final Student student = studentRepository.save(Student.builder().name("李焕英").build());
        log.info("[添加成功] - [{}]", student);
        final List<Student> studentList = studentRepository.findAll();
        log.info("[查询所有] - [{}]", studentList);
        final Student edit = studentRepository.save(new Student(student.getId(), "沈腾"));
        log.info("[修改成功] - [{}]", edit);
//        studentRepository.deleteById(student.getId());
//        log.info("[删除主键为 {} 成功] - [{}]", student.getId());
    }

}
