package com.sun.www;

import org.springframework.core.io.ClassPathResource;

import java.lang.reflect.Method;

public class HelloClassLoader extends ClassLoader {

    public static void main(String[] args) {
        try {
            Class clazz = Class.forName("Hello", true, new HelloClassLoader());
            Method method = clazz.getMethod("hello");
            method.invoke(clazz.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Class<?> findClass(String name) {
        try {
            ClassPathResource resource = new ClassPathResource("/META-INF/Hello.xlass");
            int length = (int) resource.contentLength();
            byte[] bytes = new byte[length];
            resource.getInputStream().read(bytes, 0, length);
            bytes = decode(bytes);
            return defineClass(name, bytes, 0, bytes.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] decode(byte[] bytes) {
        byte[] newBytes = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            newBytes[i] = (byte) (255 - bytes[i]);
        }
        return newBytes;
    }

}
