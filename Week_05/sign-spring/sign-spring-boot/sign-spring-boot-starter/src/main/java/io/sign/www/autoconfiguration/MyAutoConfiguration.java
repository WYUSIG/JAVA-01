package io.sign.www.autoconfiguration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyAutoConfiguration {

    @Bean
    public Student student(){
        Student student = Student.builder().id(1).name("springboot-starter").build();
        return student;
    }
}
