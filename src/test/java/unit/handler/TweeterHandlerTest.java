package unit.handler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import handler.Handler;
import handler.TweeterHandler;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import twitter.OfficialAPI;
import twitter4j.Status;

import java.io.OutputStream;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class TweeterHandlerTest {

    @Test
    public void sendResponse() throws Exception {

        OfficialAPI twitterAPI = Mockito.mock(OfficialAPI.class);

        Status status1 = Mockito.mock(Status.class);
        when(status1.getText()).thenReturn("This is a test").thenReturn("Second test tweet");
        when(status1.getLang()).thenReturn("en").thenReturn("en");

        Status status2 = Mockito.mock(Status.class);
        when(status2.getText()).thenReturn("Second test tweet");
        when(status2.getLang()).thenReturn("en");

        when(twitterAPI.searchNumberTweetsByUser(anyInt(), anyString(), any())).thenReturn(Arrays
                .asList(status1, status2));

        Handler handler = new TweeterHandler("notweets", 10, twitterAPI);
        HttpExchange httpExchangeMock = Mockito.mock(HttpExchange.class);
        when(httpExchangeMock.getResponseHeaders()).thenReturn(Mockito.mock(Headers.class));
        OutputStream outputStream = Mockito.mock(OutputStream.class);

        when(httpExchangeMock.getResponseBody()).thenReturn(outputStream);
        handler.sendResponse(httpExchangeMock);

        ArgumentCaptor<byte[]> argument = ArgumentCaptor.forClass(byte[].class);
        Mockito.verify(outputStream).write(argument.capture());
        String response = new String(argument.getValue());
        assertThat(response).isEqualTo("[\"THIS IS A TEST!\",\"SECOND TEST TWEET!\"]");

    }
}