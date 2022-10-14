package ru.netology;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
    public static void main(String[] args) {
        var server = new Server();

        server.addHandler("GET", "/messages", ((request, responseStream) -> {
            String text = "<h3>its work </h3>";
            String headers = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: " + "text/html" + "\r\n" +
                    "Content-Length: " + text.length() + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";
            request.setHeader(headers);
            try {
                responseStream.write(headers.getBytes());
                responseStream.write(text.getBytes());
                responseStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        ));

        new Thread(() -> {
            server.listen(9999);


        }).start();


        while (true) {
            try {
                server.getQueue().take().
                        getQueryParams().
                        forEach(x -> System.out.println(x.getName() + " " + x.getValue()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
