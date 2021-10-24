package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {


    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(9999);
        System.out.println("Ожидание подключения...");
        ExecutorService poll = Executors.newFixedThreadPool(64);
        while (true) {
            Socket socket = serverSocket.accept();
            Server server = new Server(socket);
            poll.execute(server);
        }
    }
}