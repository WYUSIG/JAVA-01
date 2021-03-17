package io.sign.www.rpc.annotation;

import org.springframework.stereotype.Indexed;

import java.lang.annotation.*;

/**
 * 标记需要注册到 nacos 的服务
 *
 * @author sign
 * @since 1.0
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Indexed
public @interface SignRpcService {
}
