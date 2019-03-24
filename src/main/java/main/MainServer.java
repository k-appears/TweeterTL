package main;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import handler.FactoryHandler;
import handler.Handler;
import org.apache.log4j.Logger;
import twitter4j.TwitterException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * MainServer spawns a http server using exclusively classes inside the JDK, the reason is to tune up a policy for request overflow.
 *
 * See the policy applied in {@link #getExecutor()}
 *
 */
public class MainServer {

    private static Logger logger = Logger.getLogger(MainServer.class);

    private static final String PATH_SEPARATOR = "/";
    private static final double SCALE_FACTOR = 0.7;
    private static final String PARAMETER_SEPARATOR = "&";

    private static Map<String, HttpHandler> contexts = initializeContext();
    private static HttpServer httpServer;

    /**
     * The server need to set max allowed open file to a much larger value. See http://tweaked.io/guide/kernel/
     */
    private static final int BACKLOG = 100000;

    public static final String HOST = "127.0.0.1";
    public static final int PORT = 8081;

    public static void main(String[] args) {
        start();
    }

    public static void start() {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(HOST, PORT), BACKLOG);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert httpServer != null;
        httpServer.setExecutor(getExecutor());

        for (String key : contexts.keySet()) {
            httpServer.createContext(key, contexts.get(key));
        }
        logger.info("Server started in: " + httpServer.getAddress().toString());
        httpServer.start();
    }

    public static void stop() {
        if (httpServer != null) {
            httpServer.stop(1);
        }
    }

    private static Map<String, HttpHandler> initializeContext() {
        Map<String, HttpHandler> contextMap = new HashMap<>();

        contextMap.put(PATH_SEPARATOR, exchange -> {
            List<String> segments = getPathSegments(exchange);
            String requestBody = getRequestBody(exchange);
            Map<String, List<String>> queryParameters = parseQueryParameters(exchange);
            try {
                Handler handler =
                        FactoryHandler.create(segments, queryParameters, requestBody, exchange.getRequestMethod());
                handler.sendResponse(exchange);

            } catch (TwitterException twe) {
                handleTwitterException(exchange, twe);
            } catch (IOException ex) {
                handleIOException(exchange, ex);
            } catch (Exception e) {
                logger.error("An error is caused in a unexpected condition: ");
                e.printStackTrace();
            } finally {
                exchange.close();
            }
        });

        return contextMap;
    }

    private static void handleIOException(HttpExchange exchange, Exception ex) {
        if (exchange != null) {
            try {
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, -1);
            } catch (IOException e) {
                logger.error("Can not write a response due to I/O exception as network communication");
                e.printStackTrace();
            }
        }
        logger.error("Unhandled error does not kill the server: ");
        ex.printStackTrace();
    }

    private static void handleTwitterException(HttpExchange exchange, TwitterException ex) {
        if (exchange != null) {
            try {
                byte[] out = ex.toString().getBytes(StandardCharsets.UTF_8);
                if (ex.getStatusCode() != -1) {
                    exchange.sendResponseHeaders(ex.getStatusCode(),  out.length);
                } else {
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_UNAVAILABLE,  out.length);
                }

            } catch (IOException e) {
                logger.error("Can not write a response ");
                e.printStackTrace();
            }
        }
        logger.error("Twitter API error: ");
        ex.printStackTrace();
    }

    private static String getRequestBody(HttpExchange exchange) {
        String requestBody;
        try (BufferedReader bReader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            requestBody = bReader.readLine();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return requestBody == null ? "" : requestBody;
    }

    /**
     * Each request creates a thread.
     *
     * <p>When all threads are busy then next the requests will be queued. The capacity of the queue and the number maximum of threads
     * is determined by the number of CPUs and the {@value #SCALE_FACTOR}</p>
     *
     * <p>The queued requests are processed in FIFO order</p>
     *
     * <p>When the the queued is full and a new one will be received, the executor will slow down the rate that new threads are submitted
     * until there is another thread freed, is not possible, the new request will be discarded.</p>
     *
     * @return pool of threads
     */
    private static ThreadPoolExecutor getExecutor() {
        int cpus = Runtime.getRuntime().availableProcessors();
        int maxThreads = (int) (cpus * SCALE_FACTOR);
        maxThreads = (maxThreads > 0 ? maxThreads : 1);
        return new ThreadPoolExecutor(maxThreads, // core thread pool size
                maxThreads, // maximum thread pool size
                1, // time to wait before resizing pool
                TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(maxThreads, true),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * Parse request GET query parameters of {@code ex} into a unmodifiableMap.The GET parameters are taking as UTF-8
     *
     * @param ex exchange whose request query string is to be parsed
     * @return fully-decoded parameter values
     */
    private static Map<String, List<String>> parseQueryParameters(HttpExchange ex) {
        String queryString = ex.getRequestURI().getRawQuery();
        if (queryString == null || queryString.isEmpty()) {
            return Collections.emptyMap();
        }
        if (queryString.length() > 50 + 8) { // 30 millions = 8 chars https://twittercounter.com/pages/tweets
            return Collections
                    .emptyMap(); // account no more than 50 chars https://help.twitter.com/es/managing-your-account/twitter-username-rules
        }
        Map<String, List<String>> parsedParams = new HashMap<>();
        for (String param : queryString.split(PARAMETER_SEPARATOR)) {
            String[] parts = param.split("=", 2);
            String key = parts[0];
            String value = parts.length == 2 ? parts[1] : "";
            try {
                key = URLDecoder.decode(key, StandardCharsets.UTF_8.name());
                value = URLDecoder.decode(value, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError(e);
            }
            List<String> values = parsedParams.computeIfAbsent(key, k -> new LinkedList<>());
            values.add(value);
        }

        for (Map.Entry<String, List<String>> me : parsedParams.entrySet()) {
            me.setValue(Collections.unmodifiableList(me.getValue()));
        }
        return Collections.unmodifiableMap(parsedParams);
    }

    private static List<String> getPathSegments(HttpExchange ex) {
        String path = ex.getRequestURI().getRawPath();
        List<String> listsegments = Arrays.asList(path.split(PATH_SEPARATOR));
        return Collections.unmodifiableList(listsegments.subList(1, listsegments.size()));
    }

}
