package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Request {
    private String methodName;
    private String header;
    private String body;
    private String url;
    private String fullPath;
    private String param;
    private CopyOnWriteArrayList<NameValuePair> paramList;

    public Request(String methodName, String header) {
        this.methodName = methodName;
        this.header = header;
        this.paramList = new CopyOnWriteArrayList<>();
    }

    public Request() {

    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public CopyOnWriteArrayList<NameValuePair> getParamList() {
        return paramList;
    }

    public void setParamList(CopyOnWriteArrayList<NameValuePair> paramList) {
        this.paramList = paramList;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getQueryParam(String name) {
        while (fullPath == null) {

        }
        try {
            return URLEncodedUtils.parse(new URI(fullPath), Charset.defaultCharset()).
                    stream().
                    filter(x -> x.getName().
                            equals(name)).
                    map(x -> x.getName() + " " + x.getValue()).
                    findFirst().
                    orElse("params " + name + " is not found");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<NameValuePair> getQueryParams() {
        while (fullPath == null) {

        }
        return URLEncodedUtils.parse(fullPath, Charset.defaultCharset());
    }
}
