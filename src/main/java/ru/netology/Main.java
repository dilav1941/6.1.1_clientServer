package ru.netology;

public class Main {

    public static void main ( String[] args ) {
        Server server = new Server (64);
        server.listen (9999);
        server.addHandler ("GET", "static/default-get.html", (( request, out ) -> {
            final var method = request.getMethod ();
            if (!Server.allowedMethods.contains (method)) {
                Server.badRequest (out);
                return;
            }

            final var path = request.getPath ();
            if (!path.startsWith ("/")) {
                Server.badRequest (out);
                return;
            }
            final var headers = request.getHeaders ();

            final var queryParams = request.getQueryParams ();
            out.write ((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes ());
            out.flush ();
        }
        ));
    }
}
