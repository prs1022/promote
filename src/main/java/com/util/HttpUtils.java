package com.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpUtils {

    private static Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    public static String doGet(String url, Map header, Map<String, String> params, Integer connTimeout,
                               Integer readTimeout) {
        //将params拼接在URL后边
        if (params != null && params.size() > 0) {
            List<String> tmp = new ArrayList<>();
            for (Map.Entry<String, String> param : params.entrySet()) {
                tmp.add(param.getKey() + "=" + param.getValue());
            }
            String append = StringUtils.join(tmp, "&");
            if (url.contains("?")) {
                url = url + "&" + append;
            } else {
                url = url + "?" + append;
            }
        }
        return doMethod(new HttpGet(url), null, null, header, connTimeout, readTimeout, null, null);
    }

    private static HttpClientContext getContext(String userName, String password) {
        if (userName != null) {
            CredentialsProvider provider = new BasicCredentialsProvider();
            provider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, password));
            HttpClientContext context = HttpClientContext.create();
            context.setCredentialsProvider(provider);
            return context;
        } else {
            return null;
        }
    }

    private static CloseableHttpClient getClient(final String URL) {
        if (URL.startsWith("https")) {
            return createSSLInsecureClient();
        } else {
            return HttpClients.createDefault();
        }
    }

    public static String doPost(String url, Map params, Map header) {
        return doMethod(new HttpPost(url), params, null, header, 8000, 10000, null, null);
    }

    public static String doGet(String URL, Map header) {
        return doGet(URL, header, null, null);
    }

    public static String doGet(String URL, Map header, String userName, String password) {
        return doMethod(new HttpGet(URL), null, null, header, 3000, 5000, userName, password);
    }

    private static String doMethod(HttpRequestBase requestMethod, Map param, StringEntity requestBody, Map header,
                                   Integer connTimeout, Integer readTimeout, String userName, String password) {
        if (header == null) {
            header = new HashMap();
        }
        // 设置参数
        RequestConfig.Builder customReqConf = RequestConfig.custom();
        if (connTimeout != null) {
            customReqConf.setConnectTimeout(connTimeout);
        }
        if (readTimeout != null) {
            customReqConf.setSocketTimeout(readTimeout);
        }
        //设置请求方法
        requestMethod.setConfig(customReqConf.build());
        //设置base验证
        if (StringUtils.isNotBlank(userName)) {
            String authString = userName + ":" + password;
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            String authStringEnc = new String(authEncBytes);
            header.put("Authorization", "Basic " + authStringEnc);
        }
        //设置请求头
        for (Object key : header.keySet()) {
            requestMethod.setHeader((String) key, (String) header.get(key));
        }
        try (CloseableHttpClient httpclient = getClient(requestMethod.getURI().toURL().toString());) {
            //设置请求体
            if (requestMethod instanceof HttpEntityEnclosingRequestBase) {
                if (requestBody != null) {
                    ((HttpEntityEnclosingRequestBase) requestMethod).setEntity(requestBody);
                } else if (param != null && param.size() > 0) {
                    List formParams = new ArrayList();
                    for (Object key : param.keySet()) {
                        formParams.add(new BasicNameValuePair((String) key, (String) param.get(key)));
                    }
                    ((HttpEntityEnclosingRequestBase) requestMethod).setEntity(new UrlEncodedFormEntity(formParams, "UTF-8"));
                }
            }
            //执行请求
            CloseableHttpResponse response = httpclient.execute(requestMethod);
            org.apache.http.HttpEntity entity = response.getEntity();
            StatusLine status = response.getStatusLine();
            //响应输出
            String message = "Status Code:" + status.getStatusCode() + " " + status.getReasonPhrase() + "\n";
            String content = "";
            try (InputStream is = entity.getContent()) {
                content = IOUtils.toString(is, "UTF-8");
                message += "Response Content:\n" + content;
            }
//            logger.info("url:{},method:{},response:{}", requestMethod.getURI().toURL().toString(), requestMethod.getMethod(), message);
            return content;
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            requestMethod.releaseConnection();
        }
        return null;
    }

    //post请求，body为二进制流数据
    public static String doPost(String url, String content) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        CloseableHttpResponse httpResponse = null;
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(3000).setSocketTimeout(5000).build();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);
//        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
//        multipartEntityBuilder.addBinaryBody(name, bytes);
        try {
            httpPost.setEntity(new StringEntity(content, "utf-8"));
            CloseableHttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                return EntityUtils.toString(entity);
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return null;
    }


    private static CloseableHttpClient createSSLInsecureClient() {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(
                    null, new TrustStrategy() {
                        public boolean isTrusted(X509Certificate[] chain,
                                                 String authType) throws CertificateException {
                            return true;
                        }
                    }).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    sslContext, new X509HostnameVerifier() {

                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }

                @Override
                public void verify(String host, SSLSocket ssl)
                        throws IOException {
                }

                @Override
                public void verify(String host, X509Certificate cert)
                        throws SSLException {
                }

                @Override
                public void verify(String host, String[] cns,
                                   String[] subjectAlts) throws SSLException {
                }

            });
            HttpClientBuilder builder = HttpClients.custom().setSSLSocketFactory(sslsf);
            return builder.build();
        } catch (GeneralSecurityException e) {
            logger.error("", e);
            return null;
        }
    }

    public static String doPut(String url, String body, HashMap header) {
        return doPut(url, body, header, 3000, 5000);
    }

    public static String doPut(String url, String body, HashMap header, Integer connTimeout, Integer readTimeout) {
        return doMethod(new HttpPut(url), null, new StringEntity(body, ContentType.APPLICATION_JSON), header, connTimeout, readTimeout, null, null);
    }

    public static String doDelete(String url, Map header) {
        return doMethod(new HttpDelete(url), null, null, header, 3000, 5000, null, null);
    }
}
