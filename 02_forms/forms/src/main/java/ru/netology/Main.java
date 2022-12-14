package ru.netology;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
    public static void main(String[] args) {
        final var server = new Server();
        server.addHandler("GET", "/default-get.html", (request, responseStream) -> {
            File file = new File("02_forms/forms/static/default-get.html");
            request.setHeader("HTTP/1.1 200 OK\r\n" +
                    "Content-Type: " + "text/html" + "\r\n" +
                    "Content-Length: " + file.length() + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n");
            try {
                responseStream.write(request.getHeader().getBytes());
                Files.copy(file.toPath(), responseStream);
                responseStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        Thread thread = new Thread(() -> {
            server.listen(9999);
        });

        thread.start();

    }
}
