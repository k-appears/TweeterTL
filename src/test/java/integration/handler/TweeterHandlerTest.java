package integration.handler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import handler.Handler;
import handler.TweeterHandler;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import twitter.OfficialAPI;

import java.io.OutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class TweeterHandlerTest {

    @Test
    public void sendResponse() throws Exception {
        Handler handler = new TweeterHandler("notweets", 10, new OfficialAPI());

        HttpExchange httpExchangeMock = Mockito.mock(HttpExchange.class);
        when(httpExchangeMock.getResponseHeaders()).thenReturn(Mockito.mock(Headers.class));
        OutputStream outputStream = Mockito.mock(OutputStream.class);
        when(httpExchangeMock.getResponseBody()).thenReturn(outputStream);

        handler.sendResponse(httpExchangeMock);

        ArgumentCaptor<byte[]> argument = ArgumentCaptor.forClass(byte[].class);
        Mockito.verify(outputStream).write(argument.capture());
        String response = new String(argument.getValue());
        assertThat(response).isEqualTo("[\"108 CHARACTERS LEFT... WHAT FOR?!\"]"); // see https://twitter.com/NoTweets

    }
}