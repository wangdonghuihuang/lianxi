package com.softium.datacenter.paas.web.utils;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class HttpUtilsCommon {
    private static int CONNECTION_TIME_OUT = 600000;
    private static CloseableHttpClient httpClient;
    private static Lock lock = new ReentrantLock();

    public HttpUtilsCommon() {
    }

    private static CloseableHttpClient getHttpClient() {
        if (httpClient != null) {
            return httpClient;
        } else {
            CloseableHttpClient var0;
            try {
                lock.lock();
                if (httpClient == null) {
                    HttpClientBuilder httpClientBuilder = HttpClients.custom();
                    String config = "200";
                    Integer poolSize = Integer.valueOf(config);
                    poolSize = poolSize > 1000 ? 1000 : poolSize;
                    poolSize = poolSize < 10 ? 10 : poolSize;
                    httpClientBuilder.setMaxConnTotal(poolSize);
                    httpClientBuilder.setMaxConnPerRoute(poolSize);
                    httpClientBuilder.disableCookieManagement();
                    httpClientBuilder.disableAutomaticRetries();
                    httpClientBuilder.setConnectionTimeToLive(60L, TimeUnit.SECONDS);
                    Builder builder = RequestConfig.custom();
                    builder.setConnectTimeout(CONNECTION_TIME_OUT);
                    httpClientBuilder.setDefaultRequestConfig(builder.build());
                    httpClientBuilder.setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE);
                    httpClientBuilder.disableAuthCaching();
                    httpClientBuilder.disableCookieManagement();
                    httpClientBuilder.disableRedirectHandling();
                    httpClient = httpClientBuilder.build();
                    CloseableHttpClient var4 = httpClient;
                    return var4;
                }

                var0 = httpClient;
            } finally {
                lock.unlock();
            }

            return var0;
        }
    }

    public static String get(String url, Map<String, String> headers, Map<String, String> parameterMap) throws IOException {
        CloseableHttpClient httpClient = getHttpClient();
        int i = 0;
        if (parameterMap != null) {
            for(Iterator var5 = parameterMap.entrySet().iterator(); var5.hasNext(); ++i) {
                Entry<String, String> entry = (Entry)var5.next();
                String key = URLEncoder.encode((String)entry.getKey(), "utf-8");
                String value = URLEncoder.encode((String)entry.getValue(), "utf-8");
                if (i == 0) {
                    url = url + "?" + key + "=" + value;
                } else {
                    url = url + "&" + key + "=" + value;
                }
            }
        }

        HttpGet httpGet = new HttpGet(url);
        if (headers != null) {
            Iterator var10 = headers.entrySet().iterator();

            while(var10.hasNext()) {
                Entry<String, String> entry = (Entry)var10.next();
                httpGet.addHeader((String)entry.getKey(), (String)entry.getValue());
            }
        }

        CloseableHttpResponse response = httpClient.execute(httpGet);
        return getResponseString(url, response);
    }

    private static String getResponseString(String url, CloseableHttpResponse response) throws IOException {
        String var3;
        try {
            if (response.getStatusLine().getStatusCode() != 200) {
                String error = EntityUtils.toString(response.getEntity(), "UTF-8");
                throw new RuntimeException("访问地址：" + url + "出错，错误代码：" + response.getStatusLine().getStatusCode() + "\n" + error);
            }

            HttpEntity entity = response.getEntity();
            if (entity == null) {
                var3 = null;
                return var3;
            }

            var3 = EntityUtils.toString(entity, "UTF-8");
        } finally {
            EntityUtils.consumeQuietly(response.getEntity());
        }

        return var3;
    }

    public static String post(String url, Map<String, String> headers, Map<String, String> parameterMap) throws IOException {
        CloseableHttpClient httpClient = getHttpClient();
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> nvps = new ArrayList();
        Iterator var6;
        Entry entry;
        if (parameterMap != null) {
            var6 = parameterMap.entrySet().iterator();

            while(var6.hasNext()) {
                entry = (Entry)var6.next();
                nvps.add(new BasicNameValuePair((String)entry.getKey(), (String)entry.getValue()));
            }
        }

        if (headers != null) {
            var6 = headers.entrySet().iterator();

            while(var6.hasNext()) {
                entry = (Entry)var6.next();
                httpPost.addHeader((String)entry.getKey(), (String)entry.getValue());
            }
        }

        httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
        CloseableHttpResponse response = httpClient.execute(httpPost);
        return getResponseString(url, response);
    }

    public static String put(String url, Map<String, String> headers, Map<String, String> parameterMap) throws IOException {
        CloseableHttpClient httpClient = getHttpClient();
        HttpPut put = new HttpPut(url);
        List<NameValuePair> nvps = new ArrayList();
        Iterator var6;
        Entry entry;
        if (parameterMap != null) {
            var6 = parameterMap.entrySet().iterator();

            while(var6.hasNext()) {
                entry = (Entry)var6.next();
                nvps.add(new BasicNameValuePair((String)entry.getKey(), (String)entry.getValue()));
            }
        }

        if (headers != null) {
            var6 = headers.entrySet().iterator();

            while(var6.hasNext()) {
                entry = (Entry)var6.next();
                put.addHeader((String)entry.getKey(), (String)entry.getValue());
            }
        }

        put.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
        CloseableHttpResponse response = httpClient.execute(put);
        return getResponseString(url, response);
    }

    public static String put(String url, Map<String, String> headers, String body, ContentType contentType) throws IOException {
        CloseableHttpClient httpClient = getHttpClient();
        HttpPut put = new HttpPut(url);
        StringEntity stringEntity = new StringEntity(body, contentType);
        put.setEntity(stringEntity);
        if (headers != null) {
            Iterator var7 = headers.entrySet().iterator();

            while(var7.hasNext()) {
                Entry<String, String> entry = (Entry)var7.next();
                put.addHeader((String)entry.getKey(), (String)entry.getValue());
            }
        }

        CloseableHttpResponse response = httpClient.execute(put);
        return getResponseString(url, response);
    }

    public static String post(String url, Map<String, String> headers, String body) {
        return post(url, headers, body, ContentType.APPLICATION_JSON);
    }

    public static String delete(String url, Map<String, String> headers, Map<String, String> parameterMap) throws IOException {
        CloseableHttpClient httpClient = getHttpClient();
        int i = 0;
        if (parameterMap != null) {
            for(Iterator var5 = parameterMap.entrySet().iterator(); var5.hasNext(); ++i) {
                Entry<String, String> entry = (Entry)var5.next();
                String key = URLEncoder.encode((String)entry.getKey(), "utf-8");
                String value = URLEncoder.encode((String)entry.getValue(), "utf-8");
                if (i == 0) {
                    url = url + "?" + key + "=" + value;
                } else {
                    url = url + "&" + key + "=" + value;
                }
            }
        }

        HttpDelete delete = new HttpDelete(url);
        if (headers != null) {
            Iterator var10 = headers.entrySet().iterator();

            while(var10.hasNext()) {
                Entry<String, String> entry = (Entry)var10.next();
                delete.addHeader((String)entry.getKey(), (String)entry.getValue());
            }
        }

        CloseableHttpResponse response = httpClient.execute(delete);
        return getResponseString(url, response);
    }

    public static String post(String url, Map<String, String> headers, String body, ContentType contentType) {
        CloseableHttpClient httpClient = getHttpClient();
        HttpPost httpPost = new HttpPost(url);
        StringEntity stringEntity = new StringEntity(body, contentType);
        httpPost.setEntity(stringEntity);
        Iterator var7;
        if (headers != null) {
            var7 = headers.entrySet().iterator();

            while(var7.hasNext()) {
                Entry<String, String> entry = (Entry)var7.next();
                httpPost.addHeader((String)entry.getKey(), (String)entry.getValue());
            }
        }

        var7 = null;

        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            return getResponseString(url, response);
        } catch (IOException var9) {
            var9.printStackTrace();
            return null;
        }
    }

    public static void downloadFile(String url, Map<String, String> headers, Map<String, String> parameterMap, String savedFileName) throws IOException {
        CloseableHttpClient httpClient = getHttpClient();
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> nvps = new ArrayList();
        Iterator var7;
        Entry entry;
        if (parameterMap != null) {
            var7 = parameterMap.entrySet().iterator();

            while(var7.hasNext()) {
                entry = (Entry)var7.next();
                nvps.add(new BasicNameValuePair((String)entry.getKey(), (String)entry.getValue()));
            }
        }

        if (headers != null) {
            var7 = headers.entrySet().iterator();

            while(var7.hasNext()) {
                entry = (Entry)var7.next();
                httpPost.addHeader((String)entry.getKey(), (String)entry.getValue());
            }
        }

        httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
        FileOutputStream fos = null;

        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            Throwable var9 = null;

            try {
                if (response.getStatusLine().getStatusCode() != 200) {
                    String error = EntityUtils.toString(response.getEntity(), "UTF-8");
                    throw new RuntimeException("访问地址：" + url + "出错，错误代码：" + response.getStatusLine().getStatusCode() + "\n" + error);
                }

                HttpEntity entity = response.getEntity();
                if (entity.isStreaming()) {
                    fos = new FileOutputStream(savedFileName);
                    entity.writeTo(fos);
                }
            } catch (Throwable var25) {
                var9 = var25;
                throw var25;
            } finally {
                if (response != null) {
                    if (var9 != null) {
                        try {
                            response.close();
                        } catch (Throwable var24) {
                            var9.addSuppressed(var24);
                        }
                    } else {
                        response.close();
                    }
                }

            }
        } finally {
            IOUtils.closeQuietly(fos);
        }

    }
}
