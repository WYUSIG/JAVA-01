package io.sign.www.rpc.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.sign.www.rpc.api.DefaultSignRpcResolver;
import io.sign.www.rpc.api.SignRpcRequest;
import io.sign.www.rpc.api.SignRpcResolver;
import io.sign.www.rpc.api.SignRpcResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 远程服务存根: Skeleton
 *
 * @author sign
 * @since 1.0
 */
@Component
public class SignRpcInvoker {

    @Autowired
    private SignRpcResolver resolver;

    public SignRpcInvoker(){}

    public SignRpcInvoker(SignRpcResolver resolver) {
        this.resolver = resolver;
    }

    public SignRpcResponse invoke(SignRpcRequest request) {
        SignRpcResponse response = new SignRpcResponse();
        String serviceClass = request.getServiceClass();

        Object service = resolver.resolve(serviceClass);

        try {
            Method method = getMethodFromClassAndMethodName(service.getClass(), request.getMethod());
            Object result = method.invoke(service, request.getParams());
            response.setResult(JSON.toJSONString(result, SerializerFeature.WriteClassName));
            response.setStatus(true);
            return response;
        } catch (Exception e) {
            response.setStatus(false);
            response.setException(e);
            return response;
        }
    }

    private Method getMethodFromClassAndMethodName(Class<?> clazz, String methodName) {
        return Arrays.stream(clazz.getMethods()).filter(m -> methodName.equals(m.getName())).findFirst().get();
    }
}
