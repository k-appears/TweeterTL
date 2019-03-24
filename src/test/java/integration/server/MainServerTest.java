package integration.server;

import handler.FactoryHandler;
import main.MainServer;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

public class MainServerTest {

    private static URIBuilder uriBuilder;

    @BeforeClass
    public static void setUp() {
        uriBuilder =
                new URIBuilder().setScheme(FactoryHandler.PROTOCOL).setHost(MainServer.HOST).setPort(MainServer.PORT);
        MainServer.start();
    }

    @After
    public void after() {
        uriBuilder.clearParameters();
    }

    @AfterClass
    public static void tearDown() {
        MainServer.stop();
    }

    // Given a TimelineAPI_NotToUse username and a number N, returns a JSON response with user’s last N tweets in uppercase and a final exclamation mark.
    // For example, a tweet like “Hello this is my first Tweet” would be converted to “HELLO THIS IS MY FIRST TWEET!”.

    @Test
    public void main() throws URISyntaxException, IOException {
        uriBuilder.setPath(FactoryHandler.ENDPOINT);
        uriBuilder.addParameter(FactoryHandler.USERNAME, "reuters");
        uriBuilder.addParameter(FactoryHandler.NUM_TWEETS, "10");

        JSONArray jsonArray = getTweets(new HttpGet(uriBuilder.build()));
        assertThat(jsonArray.length()).isEqualTo(10);
    }

    @Test
    public void mainPost() throws URISyntaxException, IOException {
        uriBuilder.setPath(FactoryHandler.ENDPOINT);
        uriBuilder.addParameter(FactoryHandler.USERNAME, "reuters");
        uriBuilder.addParameter(FactoryHandler.NUM_TWEETS, "10");

        assertThat(getStatusResponse(new HttpPost(uriBuilder.build()))).isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    public void mainInvalidPath() throws URISyntaxException, IOException {
        uriBuilder.setPath("/INVALID");
        uriBuilder.addParameter(FactoryHandler.USERNAME, "reuters");
        uriBuilder.addParameter(FactoryHandler.NUM_TWEETS, "10");
        assertThat(getStatusResponse(new HttpGet(uriBuilder.build()))).isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    public void mainInvalidUsername() throws URISyntaxException, IOException {
        String random = java.util.UUID.randomUUID().toString() + "random";
        uriBuilder.setPath(FactoryHandler.ENDPOINT);
        uriBuilder.addParameter(FactoryHandler.USERNAME, random);
        uriBuilder.addParameter(FactoryHandler.NUM_TWEETS, "10");
        assertThat(getStatusResponse(new HttpGet(uriBuilder.build()))).isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    public void mainInvalidFormatNumberTweets() throws URISyntaxException, IOException {
        uriBuilder.setPath(FactoryHandler.ENDPOINT);
        uriBuilder.addParameter(FactoryHandler.USERNAME, "reuters");
        uriBuilder.addParameter(FactoryHandler.NUM_TWEETS, "-1");
        assertThat(getStatusResponse(new HttpGet(uriBuilder.build()))).isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    public void mainNoUsernameParameter() throws URISyntaxException, IOException {
        uriBuilder.setPath(FactoryHandler.ENDPOINT);
        // uriBuilder.addParameter(USERNAME, "reuters");
        uriBuilder.addParameter(FactoryHandler.NUM_TWEETS, "10");
        assertThat(getStatusResponse(new HttpGet(uriBuilder.build()))).isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    public void mainNoNumberTweetsParameter() throws URISyntaxException, IOException {
        uriBuilder.setPath(FactoryHandler.ENDPOINT);
        uriBuilder.addParameter(FactoryHandler.USERNAME, "reuters");
        //uriBuilder.addParameter(NUM_TWEETS, "-1");
        assertThat(getStatusResponse(new HttpGet(uriBuilder.build()))).isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    public void mainNoUsernameNoNumberTweetsParameter() throws URISyntaxException, IOException {
        uriBuilder.setPath(FactoryHandler.ENDPOINT);
        //uriBuilder.addParameter(USERNAME, "reuters");
        //uriBuilder.addParameter(NUM_TWEETS, "-1");
        assertThat(getStatusResponse(new HttpGet(uriBuilder.build()))).isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    public void mainInvalidNumberTweetsParameter() throws URISyntaxException, IOException {
        uriBuilder.setPath(FactoryHandler.ENDPOINT);
        uriBuilder.addParameter(FactoryHandler.USERNAME, "reuters");
        uriBuilder.addParameter(FactoryHandler.NUM_TWEETS, "ShouldBeNumber");
        assertThat(getStatusResponse(new HttpGet(uriBuilder.build()))).isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    public void mainExtraArguments() throws URISyntaxException, IOException {
        uriBuilder.addParameter(FactoryHandler.USERNAME, "reuters");
        uriBuilder.addParameter(FactoryHandler.NUM_TWEETS, "10");
        uriBuilder.addParameter("AntherArgument", "10");
        assertThat(getStatusResponse(new HttpGet(uriBuilder.build()))).isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    private int getStatusResponse(HttpRequestBase httpRequestBase) throws IOException {
        try (CloseableHttpResponse response = HttpClients.createDefault().execute(httpRequestBase)) {
            try (BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
                assertThat(rd).isNotNull();
                return response.getStatusLine().getStatusCode();
            }
        }
    }

    private JSONArray getTweets(HttpRequestBase httpRequestBase) throws IOException {
        JSONArray jsonArray;
        try (CloseableHttpResponse response = HttpClients.createDefault().execute(httpRequestBase)) {
            try (BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
                assertThat(rd).isNotNull();
                String line = rd.readLine();

                assertThat(line).isNotNull();

                jsonArray = new JSONArray(line);

                assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpURLConnection.HTTP_OK);
                assertThat(ContentType.get(response.getEntity()).getMimeType())
                        .isEqualTo(ContentType.APPLICATION_JSON.getMimeType());

            }
        }
        return jsonArray;
    }

}