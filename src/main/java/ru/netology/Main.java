package ru.netology;

import java.io.*;

public class Main {


    public static void main ( String[] args ) throws IOException {
        startServer server = new startServer ();
        server.start ();
    }
}