package handler;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.HttpURLConnection;


public class ErrorHandler implements Handler{
    @Override
    public void sendResponse(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, -1);
    }

}
