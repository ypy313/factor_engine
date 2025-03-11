package com.nbcb.factor.common;

import okhttp3.*;

import java.io.IOException;

/**
 * http 工具类，获取外部信息
 */
public class HttpUtils {
    public static final MediaType JSON = MediaType.parse("application/json;charset=utf-8");

    /**
     * get 请求
     */
    public static String httpGet(String url) throws IOException{
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Response response = httpClient.newCall(request).execute();
        assert response.body() != null;
        return response.body().string();
    }

    /**
     * post 请求
     */
    public static String httpPost(String url,String json) throws IOException {
        OkHttpClient httpClient = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(json,JSON);
        Request request = new Request.Builder().url(url).post(requestBody).build();
        Response response = httpClient.newCall(request).execute();
        assert response.body() != null;
        return response.body().string();

    }
}
