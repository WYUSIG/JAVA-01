package com.google.www;

import okhttp3.*;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Future;

/**
 * 使用OkHttp访问http://localhost:8801
 *
 * @author <a href="2554136375@qq.com">钟显东</a>
 * @see HttpServer01
 */
public class OkHttpDemo {

    public static final OkHttpClient client = new OkHttpClient();
    public static final Request request = new Request.Builder().get().url(HttpServer01.URL).build();

    public static void main(String[] args) {
        get();
    }


    private static void get() {
        Call call = client.newCall(request);
        Response response = null;
        try {
            response = call.execute();
            if (response.isSuccessful()) {
                System.out.println("[OkHttp Get]状态码" + response.code());
                System.out.println("[OkHttp Get]" + Objects.requireNonNull(response.body()).string());
            } else {
                System.out.println("[OkHttp Get]请求失败");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(response != null){
//                response.close();
            }
//            call.cancel();
        }
    }


}
