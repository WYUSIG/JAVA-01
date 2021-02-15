package io.sign.www.spring.springbean;

import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * "student.xsd" {@link NamespaceHandler}实现
 * @author 钟显东
 */
public class StudentNamespaceHandler extends NamespaceHandlerSupport {

    /**
     * 将 "student" 元素注册对应的BeanDefinitionParser实现
     */
    @Override
    public void init() {
        registerBeanDefinitionParser("student",new StudentBeanDefinitionParser());
    }
}
