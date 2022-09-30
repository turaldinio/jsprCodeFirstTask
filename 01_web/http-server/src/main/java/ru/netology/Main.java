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

        server.addHandler("GET", "/messages", server::outputResponseForItsHandler);

        server.addHandler("POST", "/messages", server::outputResponseForItsHandler);

        server.listen(9999);
    }
}