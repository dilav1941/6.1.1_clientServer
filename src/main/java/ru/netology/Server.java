package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.List.*;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static final String GET = "GET";
    public static final String POST = "POST";
    private final ExecutorService executorService;
    static final List<String> allowedMethods = of (GET, POST);

    private final Map<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();

    public Server ( int poolSize ) {
        this.executorService = Executors.newFixedThreadPool (poolSize);
    }

    public void addHandler ( String method, String path, Handler handler ) {
        if (handlers.get (method) == null) {
            handlers.put (method, new ConcurrentHashMap<> ());
        }
        handlers.get (method).put (path, handler);
    }

    public void listen ( int port ) {
        try (final var serverSocket = new ServerSocket (port)) {
            while (true) {
                final var socket = serverSocket.accept ();
                executorService.submit (() -> handleConnection (socket));
            }
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }

    public void handleConnection ( Socket socket ) {
        try (
                socket;
                final var in = socket.getInputStream ();
                final var out = new BufferedOutputStream (socket.getOutputStream ())
        ) {
            var request = Request.fromInputStream (in);
            var pathHandlerMap = handlers.get (request.getMethod ());
            if (pathHandlerMap == null) {
                badRequest(out);
                return;
            }

            var handler = pathHandlerMap.get (request.getPath ());
            if (handler == null) {
                badRequest(out);
                return;
            }
            handler.handle (request, out);
        } catch (IOException e) {
            e.printStackTrace ();
        }

    }

    public static Optional<String> extractHeader ( List<String> headers, String header ) {
        return headers.stream ()
                .filter (o -> o.startsWith (header))
                .map (o -> o.substring (o.indexOf (" ")))
                .map (String::trim)
                .findFirst ();
    }

    public static void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n").getBytes());
        out.flush();
    }
}