package handler;

import com.sun.net.httpserver.HttpExchange;
import twitter4j.TwitterException;

import java.io.IOException;

/**
 * Each endpoint <code>http://host:port/path</code> will need a handler.
 * <p>
 * Each thread creates a different Handler so no synchronization is needed inside a Handler object.
 */
public interface Handler {

    /**
     * It will call {@link com.sun.net.httpserver.HttpExchange#sendResponseHeaders} to write the response
     *<p>If the call to sendResponseHeaders() specified a fixed response body length then the exact number of bytes
     *  specified in that call must be written to this stream.</p>
     *
     * <p>If too many bytes are written, then write() will throw an IOException. If too few bytes are written then
     *  the stream close() will throw an IOException.</p>
     *
     * <p>In both cases, the exchange is aborted and the underlying TCP connection closed.</p>
     *
     * @param exchange where to write the response
     *
     * @throws Exception If a handler raises any exception (checked or unchecked) the server will treat it
     */
    void sendResponse(HttpExchange exchange) throws Exception;

}
