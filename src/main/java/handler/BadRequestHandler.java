package handler;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.HttpURLConnection;


public class BadRequestHandler implements Handler {

    /**
     * {@see https://docs.oracle.com/javase/7/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/HttpExchange.html#sendResponseHeaders(int,%20long)}
     *
     * <pre> If response length has the value -1 then no response body is being sent.</pre>
     * @param exchange where to write the response
     *
     * @throws IOException
     */
    @Override
    public void sendResponse(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, -1);
    }

}
