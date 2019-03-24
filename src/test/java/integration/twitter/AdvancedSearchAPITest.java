package integration.twitter;

import me.jhenrique.manager.TweetManager;
import me.jhenrique.manager.TwitterCriteria;
import me.jhenrique.model.Tweet;
import org.junit.Ignore;
import org.junit.Test;
import twitter.AdvancedSearchAPI;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class AdvancedSearchAPITest {

    @Test
    public void searchNumberTweetsByUser() {
        AdvancedSearchAPI twitterSearch = new AdvancedSearchAPI();
        Supplier<TwitterCriteria> criteriaFunc = (TwitterCriteria::create);
        Function<TwitterCriteria, List<Tweet>> function = (TweetManager::getTweets);

        assertThat(twitterSearch.searchNumberTweetsByUser(20, "reuters", criteriaFunc, function)).hasSize(20)
                .allMatch(tweet -> tweet.getPermalink().contains("https://twitter.com/Reuters/"));
    }

    @Test
    public void searchNumberTweetsByUser_NoExistsUser() {
        AdvancedSearchAPI twitterSearch = new AdvancedSearchAPI();
        Supplier<TwitterCriteria> criteriaFunc = (TwitterCriteria::create);
        Function<TwitterCriteria, List<Tweet>> function = (TweetManager::getTweets);

        assertThat(twitterSearch
                .searchNumberTweetsByUser(1, UUID.randomUUID().toString() + "ramdon", criteriaFunc, function))
                .hasSize(0);
    }

    @Test
    public void searchNumberTweetsByUser_ZeroRequestedTweets() {
        AdvancedSearchAPI twitterSearch = new AdvancedSearchAPI();
        Supplier<TwitterCriteria> criteriaFunc = (TwitterCriteria::create);
        Function<TwitterCriteria, List<Tweet>> function = (TweetManager::getTweets);

        assertThat(twitterSearch.searchNumberTweetsByUser(0, "reuters", criteriaFunc, function)).hasSize(0);
    }

    @Test
    public void searchNumberTweetsByUser_NegativeRequestedTweets() {
        AdvancedSearchAPI twitterSearch = new AdvancedSearchAPI();
        Supplier<TwitterCriteria> criteriaFunc = (TwitterCriteria::create);
        Function<TwitterCriteria, List<Tweet>> function = (TweetManager::getTweets);

        assertThat(twitterSearch.searchNumberTweetsByUser(-1, "reuters", criteriaFunc, function)).hasSize(0);
    }

    //@Ignore // takes up to 2 minutes to run
    @Test
    public void searchNumberTweetsByUser_3201tweets() {
        AdvancedSearchAPI twitterSearch = new AdvancedSearchAPI();
        Supplier<TwitterCriteria> criteriaFunc = (TwitterCriteria::create);
        Function<TwitterCriteria, List<Tweet>> function = (TweetManager::getTweets);

        assertThat(twitterSearch.searchNumberTweetsByUser(3201, "reuters", criteriaFunc, function)).hasSize(3201);
    }
}