package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {
    private final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    private ConcurrentMap<String, ConcurrentMap<Handler, String>> map = new ConcurrentHashMap<>();

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

    private void processConnection(Socket socket) {
        threadPool.submit(new InternalHandler(socket));
    }

    private class InternalHandler extends Thread {
        private final Socket socket;
        private BufferedReader in;
        private BufferedOutputStream out;
        private Request request;

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

                    request = new Request();
                    request.setMethodName(parts[0]);
                    request.setUrl(parts[1]);
                    request.setFullPath("http://localhost:9999" + request.getUrl());

                    if (map.get(request.getMethodName()).containsValue(request.getUrl())) {
                        processAnAdditionalPath();
                        continue;
                    }
                    if (!validPaths.contains(request.getUrl())) {
                        reportMissingPath(out);
                        continue;
                    }
                    final var filePath = Path.of("01_web/http-server/public" + request.getUrl());
                    processAnExistingRequest(filePath, out);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        private void processAnAdditionalPath() {

            String headers = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: " + "text/html" + "\r\n" +
                    "Content-Length: " + greetings().length() + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";

            request.setHeader(headers);

            Objects.requireNonNull(map.get(request.getMethodName()).entrySet().stream().
                    filter(x -> x.getValue().contains(request.getUrl())).
                    map(Map.Entry::getKey).
                    findFirst().
                    orElse(null)).handle(request, out);

        }

        private void processAnExistingRequest(Path filePath, BufferedOutputStream out) {
            try {
                final var mimeType = Files.probeContentType(filePath);
                final var length = Files.size(filePath);

                if (filePath.getFileName().toString().equals("/classic.html")) {
                    final var template = Files.readString(filePath);
                    final var content = template.replace(
                            "{time}",
                            LocalDateTime.now().toString()
                    ).getBytes();
                    out.write((

                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + mimeType + "\r\n" +
                                    "Content-Length: " + content.length + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    out.write(content);
                    out.flush();
                    return;
                }
                out.write(("HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n").getBytes());

                Files.copy(filePath, out);
                out.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        private void reportMissingPath(BufferedOutputStream out) {//обработать ошибочный URL
            try {
                out.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private String greetings() {
            return "<h3>Hello, it's work</h3>";
        }

        public void addHandler(String methodName, String url, Handler handler) {
            if (map.get(methodName) == null) {
                ConcurrentMap<Handler, String> valueMap = new ConcurrentHashMap<>();

                valueMap.put(handler, url);
                map.put(methodName, valueMap);
            }

        }

        public void outputResponseForItsHandler(Request request, BufferedOutputStream responseStream) {
            try {
                responseStream.write(request.getHeader().getBytes());
                String text = String.format("<h3>the native handler is working!</h3></br>" +
                        "<h3>Method name %s</h3></br>" +
                        "<h3>Request Headers: %s</h3></br>" +
                        "<h3>Request URL %s </h3></br>", request.getMethodName(), request.getHeader(), request.getUrl());
                responseStream.write(text.getBytes());
                responseStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}