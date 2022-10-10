package ru.netology;


import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
    private InternalHandler internalHandler;

    private final ExecutorService threadPool = Executors.newFixedThreadPool(64,
            r -> {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true);
                return t;
            });


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
        this.internalHandler = new InternalHandler(socket);
        threadPool.submit(internalHandler);
    }


    private class InternalHandler extends Thread {
        private BufferedReader in;
        private BufferedOutputStream out;
        private Request request;

        public Request getRequest() {
            return request;
        }


        public InternalHandler(Socket socket) {
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
                    final var requestLineArray = requestLine.split(" ");

                    if (requestLineArray.length != 3) {
                        continue;
                    }

                    final var path = requestLineArray[1];
                    this.request = new Request();
                    this.request.setUrl(requestLineArray[1]);

                    this.request.setFullPath("http://localhost:9999" + requestLineArray[1]);


                    if (!map.isEmpty() &&
                            map.get(requestLineArray[0]).containsValue(new URI(request.getFullPath()).getPath())) {
                        processAnAdditionalPath(requestLineArray[0], out);
                        continue;
                    }
                    if (!validPaths.contains(path)) {
                        reportMissingPath(out);
                        continue;
                    }
                    final var filePath = Path.of("01_web/http-server/public" + path);

                    processAnExistingRequest(filePath, out);
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
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

        private String getQueryParam(String name) {
            try {
                return URLEncodedUtils.parse(new URI(request.getUrl()), Charset.defaultCharset()).stream().
                        filter(x -> x.getName().equals(name)).
                        map(x -> (x.getName() + " " + x.getValue())).
                        findFirst().
                        orElse(null);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }


        private List<NameValuePair> getQueryParams() {
            try {
                return URLEncodedUtils.parse(new URI(request.getFullPath()), StandardCharsets.UTF_8);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }


        private void processAnAdditionalPath(String methodName, BufferedOutputStream out) {
            String headers = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: " + "text/html" + "\r\n" +
                    "Content-Length: " + getText().getBytes().length + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";

            request.setMethodName(methodName);
            request.setHeader(headers);

            Objects.requireNonNull(map.get(request.getMethodName()))
                    .entrySet().
                    stream().
                    filter(x -> request.getUrl().contains(x.getValue())).
                    map(Map.Entry::getKey).
                    findFirst().
                    orElse(null).handle(request, out);

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
            responseStream.write(getText().getBytes());
            responseStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static String getText() {
        return "<h3>the native handler is working!</h3></br>";
    }

    public String getQueryParam(String name) {
        if (internalHandler.getRequest().getUrl().contains("?")) {
            return internalHandler.getQueryParam(name);
        } else {
            return null;
        }
    }

    public List<NameValuePair> getQueryParams() {
        if (internalHandler.getRequest().getUrl().contains("?")) {
            return internalHandler.getQueryParams();
        } else {
            return null;
        }
    }
}
