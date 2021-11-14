package ru.netology;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.List.of;

public class Request {
    private final String method;
    private final String path;
    private final List<String> headers;
    private final Map<String, String> queryParams;
    private final InputStream in;
    final static int limit = 4096;
    public static final String GET = "GET";
    public static final String POST = "POST";
    final static List<String> allowedMethods = of (GET, POST);

    public Request ( String method, String path, List<String> headers, Map<String, String> queryParams, InputStream in ) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.queryParams = queryParams;
        this.in = in;
    }

    public String getMethod () {
        return method;
    }

    public String getPath () {
        return path;
    }

    public List<String> getHeaders () {
        return headers;
    }

    public InputStream getIn () {
        return in;
    }

    public Map<String, String> getQueryParams () {
        return queryParams;
    }

    public static Request fromInputStream ( InputStream inputStream ) throws IOException {
        // лимит на request line + заголовки
        var in = new BufferedInputStream (inputStream);
        in.mark (limit);
        byte[] buffer = new byte[ limit ];
        int read = in.read (buffer);


        // ищем request line
        final var requestLineDelimiter = new byte[]{'\r', '\n'};
        final var requestLineEnd = indexOf (buffer, requestLineDelimiter, 0, read);
        if (requestLineEnd == -1) {
            throw new IOException ("Invalid request!");
        }

        // читаем request line
        final var requestLine = new String (Arrays.copyOf (buffer, requestLineEnd)).split (" ");
        if (requestLine.length != 3) {
            throw new IOException ("Invalid request!");
        }

        final var method = requestLine[ 0 ];
        if (!allowedMethods.contains (method)) {
            throw new IOException ("Invalid request!");
        }
        System.out.println (method);

        final var path = requestLine[ 1 ];
        if (!path.startsWith ("/")) {
            throw new IOException ("Invalid request!");
        }
        System.out.println (path);

        // ищем заголовки
        final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        final var headersEnd = indexOf (buffer, headersDelimiter, headersStart, read);
        if (headersEnd == -1) {
            throw new IOException ("Invalid request!");
        }

        // отматываем на начало буфера
        in.reset ();
        // пропускаем requestLine
        in.skip (headersStart);

        final var headersBytes = in.readNBytes (headersEnd - headersStart);
        final var headers = Arrays.asList (new String (headersBytes).split ("\r\n"));
        System.out.println (headers);


        //получаем query параметры
        final var allQueryParams = Request.gerQueryParams (path);
        System.out.println (allQueryParams);

        // получаем query параметр по имени
        final var title = Request.getParamByName (allQueryParams, "title");
        final var value = Request.getParamByName (allQueryParams, "value");
        final var value1 = Request.getParamByName (allQueryParams, "value1");
        System.out.println (title);
        System.out.println (value);
        System.out.println (value1);

        return new Request (method, path, headers, allQueryParams, inputStream);
    }

    public static Map<String, String> gerQueryParams ( String path ) {
        String queryString = path.substring (2);
        Map<String, String> map = new HashMap<> ();
        if (queryString.equals ("")) {
            return map;
        }
        String[] params = queryString.split ("&");
        for (String param : params) {
            String[] keyValuePair = param.split ("=", 2);
            String name = URLDecoder.decode (keyValuePair[ 0 ], StandardCharsets.UTF_8);
            if (Objects.equals (name, "")) {
                continue;
            }
            String value = keyValuePair.length > 1 ? URLDecoder.decode (
                    keyValuePair[ 1 ], StandardCharsets.UTF_8) : "";
            map.put (name, value);
        }
        return map;
    }

    public static String getParamByName ( Map<String, String> queryParameters, String name ) {
        return queryParameters.get (name);
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

    @Override
    public String toString () {
        return "Request{" +
                "method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", headers=" + headers +
                ", queryParams=" + queryParams +
                ", in=" + in +
                '}';
    }
}