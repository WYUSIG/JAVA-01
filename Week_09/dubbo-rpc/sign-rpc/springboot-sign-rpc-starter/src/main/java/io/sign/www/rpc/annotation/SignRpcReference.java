package io.sign.www.rpc.annotation;

import org.springframework.stereotype.Indexed;

import java.lang.annotation.*;

/**
 * 服务消费者注入服务标记
 *
 * @author sign
 * @since 1.0
 **/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Indexed
public @interface SignRpcReference {
}
