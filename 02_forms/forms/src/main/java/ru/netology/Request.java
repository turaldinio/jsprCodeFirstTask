package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Request {
    private String methodName;
    private String header;
    private String body;
    private String url;
    private String fullPath;
    private List<NameValuePair> queryString = new ArrayList<>();

    public Request(String methodName, String header) {
        this.methodName = methodName;
        this.header = header;

    }

    public Request() {

    }

    public List<NameValuePair> getQueryString() {
        return queryString;
    }

    public void setQueryString(List<NameValuePair> queryString) {
        this.queryString = queryString;
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
}


