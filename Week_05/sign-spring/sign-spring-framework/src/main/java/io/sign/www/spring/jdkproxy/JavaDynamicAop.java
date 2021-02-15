package io.sign.www.spring.jdkproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class JavaDynamicAop {

    public static void main(String[] args) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Object proxy = Proxy.newProxyInstance(classLoader, new Class[]{EchoService.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (EchoService.class.isAssignableFrom(method.getDeclaringClass())) {
                    //aop before
                    System.out.println("[aop增强]执行目标方法前");
                    ProxyEchoService echoService = new ProxyEchoService(new DefaultEchoService());
                    Object result = echoService.echo((String) args[0]);
                    //aop after
                    System.out.println("[aop增强]执行目标方法后");
                    return result;
                }
                return null;
            }
        });
        EchoService echoService = (EchoService) proxy;
        echoService.echo("Hello,World");
    }
}
