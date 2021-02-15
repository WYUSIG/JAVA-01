package io.sign.www.spring.springbean;

import io.sign.www.spring.pojo.Student;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Map;

public class SpringApplication {

    @Bean
    public Student student3() {
        Student student = Student.builder().id(3).name("@Bean方式").build();
        return student;
    }


    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(SpringApplication.class);
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(applicationContext);
        reader.loadBeanDefinitions("classpath:/META-INF/spring-context.xml");
        registerBeanUseApi(applicationContext);
        applicationContext.refresh();
        //集合类型依赖查找
        Map<String, Student> studentMap = applicationContext.getBeansOfType(Student.class);
        System.out.println(studentMap);
        applicationContext.close();
    }

    public static void registerBeanUseApi(AnnotationConfigApplicationContext applicationContext) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition();
        beanDefinitionBuilder.addPropertyValue("id", 2)
                .addPropertyValue("name", "API方式");
        BeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        beanDefinition.setBeanClassName("io.sign.www.spring.pojo.Student");
        applicationContext.registerBeanDefinition("student2", beanDefinition);
    }
}
