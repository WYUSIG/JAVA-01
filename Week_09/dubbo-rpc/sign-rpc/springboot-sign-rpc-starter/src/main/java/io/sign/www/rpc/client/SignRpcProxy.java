package io.sign.www.rpc.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import io.sign.www.rpc.api.SignRpcRequest;
import io.sign.www.rpc.api.SignRpcResponse;
import io.sign.www.rpc.configuration.NacosProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.util.EntityUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.Future;

/**
 * 客户端服务接口动态代理
 *
 * @author sign
 * @since 1.0
 **/
@Slf4j
public class SignRpcProxy {

    private static CloseableHttpAsyncClient httpClient;

    private static NacosProperties nacos = null;

    static {
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);

        int cores = Runtime.getRuntime().availableProcessors();
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setConnectTimeout(10000)
                .setSoTimeout(10000)
                .setIoThreadCount(cores)
                .setRcvBufSize(32 * 1024)
                .build();
        httpClient = HttpAsyncClients.custom().setMaxConnTotal(50)
                .setMaxConnPerRoute(8)
                .setDefaultIOReactorConfig(ioReactorConfig)
                .setKeepAliveStrategy((response, context) -> 6000)
                .build();
        httpClient.start();
    }

    public static void setNacosAddress(NacosProperties nacos) {
        SignRpcProxy.nacos = nacos;
    }

    public static <T> T create(final Class<T> serviceClass) {
        return (T) Proxy.newProxyInstance(SignRpcProxy.class.getClassLoader(), new Class[]{serviceClass},
                new RpcfxInvocationHandler(serviceClass));
    }

    public static class RpcfxInvocationHandler implements InvocationHandler {

        private final Class<?> serviceClass;

        public <T> RpcfxInvocationHandler(Class<T> serviceClass) {
            this.serviceClass = serviceClass;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {

            SignRpcRequest request = new SignRpcRequest();
            request.setServiceClass(this.serviceClass.getName());
            request.setMethod(method.getName());
            request.setParams(params);

            NamingService naming = NacosFactory.createNamingService(nacos.getAddress());
            List<Instance> instanceList = naming.getAllInstances(serviceClass.getName(), nacos.getGroup());
            if (instanceList.size() == 0) {
                throw new Exception("没有可用服务：" + serviceClass.getName());
            }
            Instance instance = instanceList.get(0);
            SignRpcResponse response = post(request, instance.getIp(), instance.getPort());
            return JSON.parse(response.getResult().toString());
        }

        private SignRpcResponse post(SignRpcRequest req, String ip, int port) throws Exception {
            String url = "http://" + ip + ":" + port;
            String reqJson = JSON.toJSONString(req);
            log.info("发送rpc请求：" + req.getServiceClass());

            HttpPost httpPost = new HttpPost(url);
            StringEntity entity = new StringEntity(reqJson, "UTF-8");
            httpPost.setEntity(entity);
            Future<HttpResponse> future = httpClient.execute(httpPost, new FutureCallback<HttpResponse>() {

                @Override
                public void completed(final HttpResponse endpointResponse) {

                }

                @Override
                public void failed(final Exception ex) {
                    httpPost.abort();
                    ex.printStackTrace();
                }

                @Override
                public void cancelled() {
                    httpPost.abort();
                }
            });
            HttpResponse endpointResponse = future.get();
            if (endpointResponse.getStatusLine().getStatusCode() == 200) {
                HttpEntity httpEntity = endpointResponse.getEntity();
                if (httpEntity != null) {
                    String string = EntityUtils.toString(httpEntity, "UTF-8");
                    return JSON.parseObject(string, SignRpcResponse.class);
                }
            } else {
                throw new Exception("rpc调用网络出错，code：" + String.valueOf(endpointResponse.getStatusLine().getStatusCode()));
            }
            return null;
        }
    }
}
