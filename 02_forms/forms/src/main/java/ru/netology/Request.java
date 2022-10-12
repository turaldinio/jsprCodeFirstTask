package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Request {
    private String methodName;
    private String header;
    private String body;
    private String url;
    String fullPath;

    public Request(String methodName, String header) {
        this.methodName = methodName;
        this.header = header;
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
        try {
            return URLEncodedUtils.parse(new URI(fullPath), Charset.defaultCharset()).
                    stream().
                    filter(x -> x.getName().
                            equals(name)).
                    map(x -> x.getName() + " " + x.getValue()).
                    findFirst().
                    orElse(null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<NameValuePair> getQueryParams() {
        return URLEncodedUtils.parse(fullPath, Charset.defaultCharset());
    }
}
