package handler;

import com.sun.net.httpserver.HttpExchange;
import twitter.TwitterAPI;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Each thread creates a different Handler so no synchronization is needed
 */
public class TweeterHandler implements Handler {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_VALUE = "application/json; charset=utf-8";
    private static final String O_AUTH_CONSUMER_SECRET = "OAuthConsumerSecret";
    private static final String O_AUTH_CONSUMER_KEY1 = "OAuthConsumerKey";
    private static final String O_AUTH_CONSUMER_KEY = O_AUTH_CONSUMER_KEY1;
    private static final String O_AUTH_ACCESS_TOKEN = "OAuthAccessToken";
    private static final String O_AUTH_ACCESS_TOKEN_SECRET = "OAuthAccessTokenSecret";
    private static final String TWEETER_PROPERTIES = "tweeter.properties";

    private final String userName;
    private final int numLatestPost;
    private final TwitterAPI twitterAPI;
    private final Twitter twitter;

    public TweeterHandler(String userName, int numLatestPost, TwitterAPI twitterAPI) {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        if (System.getProperty(O_AUTH_CONSUMER_SECRET) != null && System.getProperty(O_AUTH_CONSUMER_KEY) != null
                && System.getProperty(O_AUTH_ACCESS_TOKEN) != null
                && System.getProperty(O_AUTH_ACCESS_TOKEN_SECRET) != null) {
            cb.setDebugEnabled(true).setPrettyDebugEnabled(true)
                    .setOAuthConsumerKey(System.getProperty(O_AUTH_CONSUMER_KEY))
                    .setOAuthConsumerSecret(System.getProperty(O_AUTH_CONSUMER_SECRET))
                    .setOAuthAccessToken(System.getProperty(O_AUTH_ACCESS_TOKEN))
                    .setOAuthAccessTokenSecret(System.getProperty(O_AUTH_ACCESS_TOKEN_SECRET));
        } else {

            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Properties props = new Properties();
            try(InputStream resourceStream = loader.getResourceAsStream(TWEETER_PROPERTIES)) {
                props.load(resourceStream);
                cb.setDebugEnabled(true).setPrettyDebugEnabled(true)
                        .setOAuthConsumerKey(props.getProperty(O_AUTH_CONSUMER_KEY))
                        .setOAuthConsumerSecret(props.getProperty(O_AUTH_CONSUMER_SECRET))
                        .setOAuthAccessToken(props.getProperty(O_AUTH_ACCESS_TOKEN))
                        .setOAuthAccessTokenSecret(props.getProperty(O_AUTH_ACCESS_TOKEN_SECRET));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        TwitterFactory tf = new TwitterFactory(cb.build());
        this.twitter = tf.getInstance();

        this.numLatestPost = numLatestPost;
        this.userName = userName;
        this.twitterAPI = twitterAPI;
    }

    /**
     * {@link twitter4j.TwitterException} happens when errors with twitter as IOException or surpass quota
     */
    @Override
    public void sendResponse(HttpExchange exchange) throws IOException, TwitterException {

        exchange.getResponseHeaders().add(CONTENT_TYPE, CONTENT_TYPE_VALUE);

        List<Status> statuses = twitterAPI.searchNumberTweetsByUser(numLatestPost, userName, () -> twitter);

        List<String> tweets =
                statuses.stream().map(status -> status.getText().toUpperCase(new Locale(status.getLang())) + "!")
                        .collect(Collectors.toList());
        String response = new JSONArray(tweets).toString();

        writeResponse(exchange, response);
    }

    static void writeResponse(HttpExchange exchange, String response) throws IOException {
        byte[] out = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, out.length);

        OutputStream os = exchange.getResponseBody();
        os.write(out);
        os.close();
    }

}
