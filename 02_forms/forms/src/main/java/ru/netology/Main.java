package ru.netology;

public class Main {
    public static void main(String[] args) {
        final var server = new Server();

        server.addHandler("GET", "/messages", server::outputResponseForItsHandler);

        server.addHandler("POST", "/messages", server::outputResponseForItsHandler);

        server.listen(9999);
    }
}