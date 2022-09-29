package ru.netology;

public class Request {
    private String methodName;
    private String header;
    String body;

    public Request(String methodName, String header) {
        this.methodName = methodName;
        this.header = header;
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
