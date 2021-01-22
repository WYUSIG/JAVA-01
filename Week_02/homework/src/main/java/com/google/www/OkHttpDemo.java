package com.google.www;

import com.google.www.httpserver.HttpServer04;
import okhttp3.*;

import java.util.Objects;

/**
 * 使用OkHttp访问http://localhost:8801
 *
 * @author <a href="2554136375@qq.com">钟显东</a>
 * @see HttpServer04
 */
public class OkHttpDemo {

    public static void main(String[] args) throws Exception {
        get();
        post();
    }


    private static void get() throws Exception{
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().get().url(HttpServer04.URL).build();
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
        }finally {
            if(response != null){
                response.close();
            }
            call.cancel();
        }
    }


    private static void post() throws Exception{
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder().build();
        Request request = new Request.Builder().post(requestBody).url(HttpServer04.URL).build();
        Call call = client.newCall(request);
        Response response = null;
        try {
            response = call.execute();
            if (response.isSuccessful()) {
                System.out.println("[OkHttp Post]状态码" + response.code());
                System.out.println("[OkHttp Post]" + Objects.requireNonNull(response.body()).string());
            } else {
                System.out.println("[OkHttp Post]请求失败");
            }
        }finally {
            if(response != null){
                response.close();
            }
            call.cancel();
        }
    }
}
