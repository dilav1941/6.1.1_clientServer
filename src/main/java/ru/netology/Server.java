package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static java.util.List.*;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static final String GET = "GET";
    public static final String POST = "POST";
    private Socket socket;
    final List<String> allowedMethods = of (GET, POST);
    final int limit = 4096;


    public Server ( Socket socket ) {
        this.socket = socket;
    }

    public void startServer () {
        final int SERVER_PORT = 9999;
        final ExecutorService poll = Executors.newFixedThreadPool (64);

        try (var serverSocket = new ServerSocket (SERVER_PORT)) {
            while (true) {
                Socket socket = serverSocket.accept ();
                poll.execute (()->this.request(socket));
            }
        } catch (IOException ex) {
            ex.getMessage ();
        }
        System.out.println ("Ожидание подключения...");
    }

    final List<String> validPaths = of ("/index.html", "/spring.svg",
            "/spring.png", "/resources.html", "/styles.css", "/app.js",
            "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");


    public void request (Socket socket) {
        try {
            final var in = new BufferedInputStream (this.socket.getInputStream ());
            final var out = new BufferedOutputStream (this.socket.getOutputStream ());

            // лимит на request line + заголовки
            in.mark (limit);
            byte[] buffer = new byte[ limit ];
            int read = in.read (buffer);

            // ищем request line
            final var requestLineDelimiter = new byte[]{'\r', '\n'};
            final var requestLineEnd = indexOf (buffer, requestLineDelimiter, 0, read);
            if (requestLineEnd == -1) {
                badRequest (out);

            }

            // читаем request line
            final var requestLine = new String (Arrays.copyOf (buffer, requestLineEnd)).split (" ");
            if (requestLine.length != 3) {
                badRequest (out);
                return;

            }

            final var method = requestLine[ 0 ];
            if (!allowedMethods.contains (method)) {
                badRequest (out);
                return;
            }
            System.out.println (method);

            final var path = requestLine[ 1 ];
            if (!path.startsWith ("/")) {
                badRequest (out);
                return;
            }
            System.out.println (path);
            // получаем queryString из path
            String queryString = Request.queryString (path);
            // парсим queryString в map
            final var map = Request.parseQueryString (queryString);
            //демонстрация получения значения параметра по его имени
            System.out.println (Request.getParamByName (map, "title"));
            System.out.println (Request.getParamByName (map, "value"));
            System.out.println (Request.getParamByName (map, "value1"));


            // ищем заголовки
            final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
            final var headersStart = requestLineEnd + requestLineDelimiter.length;
            final var headersEnd = indexOf (buffer, headersDelimiter, headersStart, read);
            if (headersEnd == -1) {
                badRequest (out);
            }

            // отматываем на начало буфера
            in.reset ();
            // пропускаем requestLine
            in.skip (headersStart);

            final var headersBytes = in.readNBytes (headersEnd - headersStart);
            final var headers = Arrays.asList (new String (headersBytes).split ("\r\n"));
            System.out.println (headers);

            // для GET тела нет
            if (!method.equals (GET)) {
                in.skip (headersDelimiter.length);
                // вычитываем Content-Length, чтобы прочитать body
                final var contentLength = extractHeader (headers, "Content-Length");
                if (contentLength.isPresent ()) {
                    final var length = Integer.parseInt (contentLength.get ());
                    final var bodyBytes = in.readNBytes (length);

                    final var body = new String (bodyBytes);
                    System.out.println (body);
                }
            }

            out.write ((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes ());
            out.flush ();
        } catch (IOException e) {
            e.printStackTrace ();
        }

    }


    private static Optional<String> extractHeader ( List<String> headers, String header ) {
        return headers.stream ()
                .filter (o -> o.startsWith (header))
                .map (o -> o.substring (o.indexOf (" ")))
                .map (String::trim)
                .findFirst ();
    }

    private static void badRequest ( BufferedOutputStream out ) throws IOException {
        out.write ((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes ());
        out.flush ();
    }

    // from google guava with modifications
    private static int indexOf ( byte[] array, byte[] target, int start, int max ) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[ i + j ] != target[ j ]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }


    public List<String> getValidPaths () {
        return validPaths;
    }
}