package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        final var server = new Server();

        server.addHandler("GET", "/messages", (request, responseStream) -> {
            try {
                responseStream.write(request.getHeader().getBytes());
                String text = "<h1>the native handler is working " + request.getMethodName() + "</h1>";
                responseStream.write(text.getBytes());
                responseStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

//        server.addHandler("POST", "/messages", (request, responseStream) -> {
//            try {
//                responseStream.write(request.getHeader().getBytes());
//                String text = "<h1>the native handler is working" + request.getMethodName() + "</h1>";
//                responseStream.write(text.getBytes());
//                responseStream.flush();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });

        server.listen(9999);
    }
}