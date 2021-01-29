package com.alibaba.www.util;

import com.alibaba.www.Application;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @ClassName SpringUtil
 * @Description: TODO
 * @Author 钟显东
 * @Date 2021/1/29 0029
 * @Version V1.0
 **/
public class SpringUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        applicationContext = ac;
    }

    public static Object getBean(Class clazz) {
        return applicationContext.getBean(clazz);
    }
}
