package ru.netology;

import java.net.Socket;

public class Main {

    public static void main ( String[] args ){
        Socket socket = null;
        Server server = new Server (socket);
        server.startServer ();
    }
}
