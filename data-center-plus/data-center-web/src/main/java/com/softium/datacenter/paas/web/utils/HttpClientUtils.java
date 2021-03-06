package com.softium.datacenter.paas.web.utils;

import org.apache.commons.codec.Charsets;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;

public class HttpClientUtils {
    private static PoolingHttpClientConnectionManager cm;
    private static String UTF_8 = "UTF-8";

    private static CloseableHttpClient httpClient;

    static{
        init();
    }


    private static void init() {
        if (cm == null) {
            cm = new PoolingHttpClientConnectionManager();
            cm.setMaxTotal(50);// 整个连接池最大连接数
            cm.setDefaultMaxPerRoute(5);// 每路由最大连接数，默认值是2
        }
        httpClient= HttpClients.custom().setConnectionManager(cm).build();
    }
    /**
     * 通过连接池获取HttpClient
     *
     * @return
     */
    private static CloseableHttpClient getHttpClient() {
//        RequestConfig config = RequestConfig.custom().setRedirectsEnabled(true).build();//允许重定向
//        return HttpClients.custom().setDefaultRequestConfig(config).setConnectionManager(cm).build();
//        return HttpClients.custom().setConnectionManager(cm).build();
        return httpClient;
    }

    /**
     *
     * @param url
     * @return
     */
    public static String get(String url) {
        HttpGet httpGet = new HttpGet(url);
        return getResult(httpGet);
    }

    /**
     *
     * @param url
     * @return
     */
    public static byte[] getBinary(String url) {
        HttpGet httpGet = new HttpGet(url);
        return getBinaryResult(httpGet);
    }

    public static String get(String url, Map<String, Object> params) throws URISyntaxException {
        URIBuilder ub = new URIBuilder();
        ub.setPath(url);

        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        ub.setParameters(pairs);

        HttpGet httpGet = new HttpGet(ub.build());
        return getResult(httpGet);
    }

    public static String get(String url, Map<String, Object> headers, Map<String, Object> params)
            throws URISyntaxException {
        URIBuilder ub = new URIBuilder();
        ub.setPath(url);

        if (params!=null) {
            ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
            ub.setParameters(pairs);
        }
        HttpGet httpGet = new HttpGet(ub.build());
        for (Map.Entry<String, Object> param : headers.entrySet()) {
            httpGet.addHeader(param.getKey(), String.valueOf(param.getValue()));
        }
        return getResult(httpGet);
    }

    public static String post(String url) {
        HttpPost httpPost = new HttpPost(url);
        return getResult(httpPost);
    }

    public static String post(String url, Map<String, Object> params) throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url);
        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));
        return getResult(httpPost);
    }

    public static String post(String url, Map<String, Object> headers, Map<String, Object> params)
            throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url);

        for (Map.Entry<String, Object> param : headers.entrySet()) {
            httpPost.addHeader(param.getKey(), String.valueOf(param.getValue()));
        }

        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));

        return getResult(httpPost);
    }

    private static ArrayList<NameValuePair> covertParams2NVPS(Map<String, Object> params) {
        ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            pairs.add(new BasicNameValuePair(param.getKey(), String.valueOf(param.getValue())));
        }
        return pairs;
    }

    /**
     * 处理Http请求
     *
     * @param request
     * @return
     */
    private static String getResult(HttpRequestBase request) {
        // CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpClient httpClient = getHttpClient();
        try {

            CloseableHttpResponse response = httpClient.execute(request);
            // response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();

            //多次重定向处理
            if ( response.getStatusLine().getStatusCode() == 302) {
                Header[] header = response.getHeaders("location");
                return get(header[0].getValue());
            }

            if (entity != null) {
                // long len = entity.getContentLength();// -1 表示长度未知
                String result = EntityUtils.toString(entity,"UTF-8");
                response.close();
                // httpClient.close();
                return result;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
        return "";
    }

    /**
     * 处理Http请求 ,返回二进制数据
     *
     * @param request
     * @return
     */
    private static byte[] getBinaryResult(HttpRequestBase request) {
        // CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpClient httpClient = getHttpClient();
        try {

            CloseableHttpResponse response = httpClient.execute(request);
            // response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();

            //多次重定向处理
            if ( response.getStatusLine().getStatusCode() == 302) {
                Header[] header = response.getHeaders("location");
                return getBinary(header[0].getValue());
            }

            if (entity != null) {
                // long len = entity.getContentLength();// -1 表示长度未知
                byte[] result = EntityUtils.toByteArray(entity);
                response.close();
                // httpClient.close();
                return result;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
        return null;
    }


    /**
     * post 请求
     * @param url
     * @param params
     * @param headers
     * @return
     * @throws Exception
     */
    public static String httpPostRequest(String url, String params, Map<String, Object> headers) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        if(null != headers){
            for (Map.Entry<String, Object> param : headers.entrySet()) {
                httpPost.setHeader(param.getKey(), String.valueOf(param.getValue()));
            }
        }
        StringEntity se = new StringEntity(params, "UTF-8");
        //se.setContentType("application/json");
        httpPost.setEntity(se);
        return getResult(httpPost);
    }
    public static String sendHttpPost(String url, String body) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
        httpPost.setHeader("Accept", "application/json");
        httpPost.setEntity(new StringEntity(body, Charsets.UTF_8));
        CloseableHttpResponse response = httpClient.execute(httpPost);
        System.out.println(response.getStatusLine().getStatusCode() + "\n");
        HttpEntity entity = response.getEntity();
        String responseContent = EntityUtils.toString(entity, "UTF-8");
        response.close();
        httpClient.close();
        return responseContent;
    }
}
