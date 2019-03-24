package handler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;


/**
 * Each thread creates a different Handler so no synchronization is needed
 */
public class LoginHandler implements Handler {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String TEXT_PLAIN = "text/plain";

    LoginHandler(String userName, int numLatestPost) {
        //TODO
    }

    @Override
    public void sendResponse(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add(CONTENT_TYPE, TEXT_PLAIN);

        // TODO
        String response = "TODO";
        TweeterHandler.writeResponse(exchange, response);
    }

}
