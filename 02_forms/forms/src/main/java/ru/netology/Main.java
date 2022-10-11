package ru.netology;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
    public static void main(String[] args) {
        final var server = new Server();
        server.listen(9999);

    }

}
