package io.sign.www.rpc.client;

//import io.sign.www.rpc.annotation.SignRpcInject;
import io.sign.www.rpc.configuration.SignRpcProperties;
import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * @ClassName MyBeanFactoryPostProcessor
 * @Description: TODO
 * @Author 钟显东
 * @Date 2021/3/17 0017
 * @Version V1.0
 **/
@Component
public class MyBeanFactoryPostProcessor implements BeanFactoryPostProcessor {


    @SneakyThrows
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
//        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition();
//        Class clazz = Class.forName("io.sign.www.rpc.UserService");
//        beanDefinitionBuilder.setFactoryMethod(SignRpcProxy.create(clazz));
//        BeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
////非命名方式
//        BeanDefinitionReaderUtils.registerWithGeneratedName(beanDefinition,applicationContext);
//        Iterator<String> iterator = beanFactory.getBeanNamesIterator();
//        while (iterator.hasNext()) {
//            String beanName = iterator.next();
//            try {
//                BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
//                Class clazz = Class.forName(bd.getBeanClassName());
//                Stream.of(clazz.getDeclaredFields())
//                        .filter(field -> {
//                            int mods = field.getModifiers();
//                            return !Modifier.isStatic(mods) &&
//                                    field.isAnnotationPresent(SignRpcInject.class);
//                        }).forEach(field -> {
//                    field.setAccessible(true);
//                    try {
//                        // 注入代理对象
//                        field.set(beanFactory.getBean(beanName), SignRpcProxy.create(field.getDeclaringClass()));
//                    } catch (IllegalAccessException e) {
//                    }
//                });
//            }catch (Exception e){
//                continue;
//            }
//        }
        System.out.println("111");
    }
}
