package ru.netology;

import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
    public static void main(String[] args) {
        final var server = new Server();
        server.addHandler("GET", "/messages", server::outputResponseForItsHandler);

        server.addHandler("GET", "/default-get.html", ((request, responseStream) -> {
            File file = new File("02_forms/forms/static/default-get.html");
            try {
                String header = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + Files.probeContentType(file.toPath()) + "\r\n" +
                        "Content-Length: " + file.length() + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n";
                responseStream.write(header.getBytes());
                Files.copy(file.toPath(), responseStream);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }));


        Thread thread = new Thread(() -> {
            server.listen(9999);
        });

        thread.setDaemon(true);
        thread.start();

        CloseableHttpClient httpClient = HttpClientBuilder.create().
                setUserAgent("MyTestService").
                setDefaultRequestConfig(RequestConfig.custom().
                        setConnectTimeout(5000).
                        setSocketTimeout(30000).
                        setRedirectsEnabled(false).
                        build()).
                build();

        HttpGet request = new HttpGet("http://localhost:9999/messages?name=turaldinio&value=15");
        request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());


        try {
            httpClient.execute(request);

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("search for name:");
        System.out.println(server.getQueryParam("name"));
        System.out.println("-------------------------------");
        System.out.println("allParams:");
        server.getQueryParams().forEach((x -> System.out.println(x.getName() + " " + x.getValue())));

    }

}
