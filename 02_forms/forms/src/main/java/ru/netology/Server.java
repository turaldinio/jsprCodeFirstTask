package ru.netology;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;


public class Server {
    private final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    private ConcurrentMap<String, ConcurrentMap<Handler, String>> map = new ConcurrentHashMap<>();
    private final Request request;
    private final ExecutorService executorService = Executors.newFixedThreadPool(64);
    private ArrayBlockingQueue<Request> queue = new ArrayBlockingQueue<>(1);

    public ArrayBlockingQueue<Request> getQueue() {
        return queue;
    }

    public void setQueue(ArrayBlockingQueue<Request> queue) {
        this.queue = queue;
    }

    public Server() {
        this.request = new Request();
    }


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
        Future<Request> future = executorService.submit(new InternalHandler(socket));
        try {
            Request u = future.get();
            queue.put(u);
        } catch (InterruptedException | ExecutionException e) {
            return;
        }

    }

    private class InternalHandler implements Callable<Request> {
        private BufferedReader in;
        private BufferedOutputStream out;

        public InternalHandler(Socket socket) {
            try {
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.out = new BufferedOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public Request call() {
            try {
                String requestLine = in.readLine();
                var parts = requestLine.split(" ");

                if (parts.length != 3) {
                    return null;
                }
                request.setUrl(parts[1]);
                request.setMethodName(parts[0]);
                request.setFullPath("http://localhost:9999" + request.getUrl());

                if (!map.isEmpty() &&
                        map.get(request.getMethodName()).containsValue(new URI(request.getFullPath()).getPath())) {
                    processAnAdditionalPath();
                    return request;
                }
                if (!validPaths.contains(request.getUrl())) {
                    reportMissingPath(out);
                    return request;
                }
                final var filePath = Path.of("01_web/http-server/public" + request.getUrl());
                processAnExistingRequest(filePath, out);
                return request;

            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
            return request;
        }


        private void processAnAdditionalPath() {
            Objects.requireNonNull(map.get(request.getMethodName()).entrySet().stream().
                    filter(x -> request.getUrl().contains(x.getValue())).
                    map(Map.Entry::getKey).
                    findFirst().
                    orElse(null)).handle(request, out);

        }

        private void processAnExistingRequest(Path filePath, BufferedOutputStream out) {
            try {
                var mimeType = Files.probeContentType(filePath);
                var length = Files.size(filePath);

                if (filePath.getFileName().toString().equals("/classic.html")) {
                    var template = Files.readString(filePath);
                    var content = template.replace(
                            "{time}",
                            LocalDateTime.now().toString()
                    ).getBytes();

                    String headers = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + content.length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n";
                    request.setHeader(headers);

                    out.write((headers).getBytes());
                    out.write(content);
                    out.flush();
                    return;
                }
                String headers = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n";
                request.setHeader(headers);

                out.write((headers).getBytes());
                Files.copy(filePath, out);
                out.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        private void reportMissingPath(BufferedOutputStream out) {//обработать ошибочный URL
            String header = "HTTP/1.1 404 Not Found\r\n" +
                    "Content-Length: 0\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";

            request.setHeader(header);
            try {
                out.write((header).getBytes());
                out.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addHandler(String methodName, String url, Handler handler) {
        if (map.get(methodName) == null) {
            ConcurrentMap<Handler, String> valueMap = new ConcurrentHashMap<>();

            valueMap.put(handler, url);
            map.put(methodName, valueMap);
        }
    }
}
