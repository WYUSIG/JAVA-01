package io.sign.www.springbootdemo;

import io.sign.www.autoconfiguration.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class SpringBootDemoApplication {

    @Autowired
    private Student student;

    @PostConstruct
    public void init(){
        System.out.println(student);
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringBootDemoApplication.class, args);
    }

}
