package com.google.www;

import com.google.www.httpserver.HttpServer04;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

/**
 * 使用HttpClient访问http://localhost:8801
 *
 * @author <a href="2554136375@qq.com">钟显东</a>
 * @see HttpServer04
 */
public class HttpClientDemo {


    public static void main(String[] args) throws Exception {
        get();
        post();
    }

    private static void get() throws Exception {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(HttpServer04.URL);
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
            HttpEntity responseEntity = response.getEntity();
            System.out.println("[HttpClient Get]响应状态为:" + response.getStatusLine());
            if (responseEntity != null) {
                System.out.println("[HttpClient Get]响应内容为:" + EntityUtils.toString(responseEntity));
            }
        } finally {
            if (httpClient != null) {
                httpClient.close();
            }
            if (response != null) {
                response.close();
            }
        }
    }


    private static void post() throws Exception {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(HttpServer04.URL);
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            System.out.println("[HttpClient Post]响应状态为:" + response.getStatusLine());
            if (responseEntity != null) {
                System.out.println("[HttpClient Post]响应内容为:" + EntityUtils.toString(responseEntity));
            }
        } finally {
            if (httpClient != null) {
                httpClient.close();
            }
            if (response != null) {
                response.close();
            }
        }
    }
}
