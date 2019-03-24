package integration.twitter;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import twitter.OfficialAPI;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class OfficialAPITest {

    private static Twitter twitter;

    @BeforeClass
    public static void beforeClass() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        // See README.md
        cb.setDebugEnabled(true).setPrettyDebugEnabled(true).setOAuthConsumerKey("0t1Nn2LzqCa33hUBaqy4Dz2YX")
                .setOAuthConsumerSecret("w7Nhudjqgrj8ZMwa3or11aJ7jS7bazyjX45MDNTVi7QP7R7oyb")
                .setOAuthAccessToken("139700982-SJSq4qnKMvdxiW9GCb1w77lHi7fHhbt01VQMFo3j")
                .setOAuthAccessTokenSecret("k2N55LbxZouNmmIWjkVPYCEpromse9hOmGfiaXsyatJjJ");
        TwitterFactory tf = new TwitterFactory(cb.build());
        twitter = tf.getInstance();
    }

    @Test
    public void searchNumberTweetsByUser() throws TwitterException {
        OfficialAPI officialAPI = new OfficialAPI();
        assertThat(officialAPI.searchNumberTweetsByUser(1, "reuters", () -> twitter)).hasSize(1).allMatch(
                status -> Arrays.stream(status.getMediaEntities())
                        .allMatch(e -> e.getExpandedURL().startsWith("https://twitter.com/Reuters/")));
    }

    @Test
    public void searchNumberTweetsByUser_NoExistsUser() {
        OfficialAPI officialAPI = new OfficialAPI();
        assertThatExceptionOfType(TwitterException.class).isThrownBy(() -> officialAPI
                .searchNumberTweetsByUser(1, UUID.randomUUID().toString() + "ramdon", () -> twitter))
                .withMessageContaining("Sorry, that page does not exist.");

    }

    @Test
    public void searchNumberTweetsByUser_ZeroRequestedTweets() throws TwitterException {
        OfficialAPI officialAPI = new OfficialAPI();
        assertThat(officialAPI.searchNumberTweetsByUser(0, "reuters", () -> twitter)).hasSize(0);
    }

    @Test
    public void searchNumberTweetsByUser_NegativeRequestedTweets() throws TwitterException {
        OfficialAPI officialAPI = new OfficialAPI();
        assertThat(officialAPI.searchNumberTweetsByUser(-1, "reuters", () -> twitter)).hasSize(0);
    }

    // @Ignore
    @Test
    public void searchNumberTweetsByUser3199() throws TwitterException {
        OfficialAPI officialAPI = new OfficialAPI();
        assertThat(officialAPI.searchNumberTweetsByUser(3199, "reuters", () -> twitter)).hasSize(3199);
    }

    // @Ignore
    @Test
    public void searchNumberTweetsByUser3200() throws TwitterException {
        OfficialAPI officialAPI = new OfficialAPI();
        assertThat(officialAPI.searchNumberTweetsByUser(3200, "reuters", () -> twitter)).hasSize(3200);
    }

    @Test
    public void searchNumberTweetsByUser3201() {
        OfficialAPI officialAPI = new OfficialAPI();
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> officialAPI.searchNumberTweetsByUser(3201, "reuters", () -> twitter));
    }
}