package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    private ConcurrentMap<String, ConcurrentMap<String, Handler>> map = new ConcurrentHashMap<>();

    private final ExecutorService threadPool = Executors.newFixedThreadPool(64);

    public void listen(int port) {
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                final var socket = serverSocket.accept();
                processConnection(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processConnection(Socket socket) {
        threadPool.submit(new InternalHandler(socket));

    }

    private class InternalHandler extends Thread {
        private final Socket socket;
        private BufferedReader in;
        private BufferedOutputStream out;


        public InternalHandler(Socket socket) {
            this.socket = socket;
            try {
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.out = new BufferedOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    final String requestLine = in.readLine();
                    final var parts = requestLine.split(" ");

                    if (parts.length != 3) {
                        continue;
                    }

                    final var path = parts[1];
                    if (!validPaths.contains(path)) {
                        out.write((
                                "HTTP/1.1 404 Not Found\r\n" +
                                        "Content-Length: 0\r\n" +
                                        "Connection: close\r\n" +
                                        "\r\n"
                        ).getBytes());
                        out.flush();
                        continue;
                    }

                    final var filePath = Path.of("01_web/http-server/public" + path);
                    final var mimeType = Files.probeContentType(filePath);
                    final var length = Files.size(filePath);


                    if (path.equals("/classic.html")) {
                        final var template = Files.readString(filePath);
                        final var content = template.replace(
                                "{time}",
                                LocalDateTime.now().toString()
                        ).getBytes();
                        String headers = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + content.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n";
                        Request request = new Request(parts[0], headers);
                        request.setUrl(parts[1]);
                        map.get(request.getMethodName()).get(filePath.toString()).
                                handle(request, new BufferedOutputStream(socket.getOutputStream()));

                        continue;
                    }
                    String headers = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n";

                    Request request = new Request(parts[0], headers);
                    request.setUrl(parts[1]);
                    map.get(request.getMethodName()).get(path).
                            handle(request, out);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void addHandler(String methodName, String url, Handler handler) {
        if (map.get(methodName) == null) {
            ConcurrentMap<String, Handler> valueMap = new ConcurrentHashMap<>();

            valueMap.put(url, handler);
            map.put(methodName, valueMap);
        }

    }
}