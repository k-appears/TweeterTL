package integration.twitter;

import org.junit.Test;
import twitter.ScrollDownAPI;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class ScrollDownAPITest {

    @Test
    public void searchNumberTweetsByUser() throws IOException {
        ScrollDownAPI scrollDownAPI = new ScrollDownAPI();
        assertThat(scrollDownAPI.searchNumberTweetsByUser(41, "reuters")).hasSize(41).extracting("permalink")
                .allMatch(s -> ((String) s).contains("reuters"));
    }

    @Test
    public void searchNumberTweetsByUser_NoExistsUser() {
        ScrollDownAPI scrollDownAPI = new ScrollDownAPI();
        String random = java.util.UUID.randomUUID().toString() + "random";
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> scrollDownAPI.searchNumberTweetsByUser(1, random)).withMessageContaining(random);
    }

    @Test
    public void searchNumberTweetsByUser_ZeroRequestedTweets() throws IOException {
        ScrollDownAPI scrollDownAPI = new ScrollDownAPI();
        assertThat(scrollDownAPI.searchNumberTweetsByUser(0, "reuters")).hasSize(0);
    }

    @Test
    public void searchNumberTweetsByUser_NegativeRequestedTweets() throws IOException {
        ScrollDownAPI scrollDownAPI = new ScrollDownAPI();
        assertThat(scrollDownAPI.searchNumberTweetsByUser(-1, "reuters")).hasSize(0);
    }

    @Test
    public void searchNumberTweetsByUser_LimitIs830() throws IOException {
        ScrollDownAPI scrollDownAPI = new ScrollDownAPI();
        assertThat(scrollDownAPI.searchNumberTweetsByUser(3200, "nytimes").size())
                .isBetween(800, 870);
    }


}