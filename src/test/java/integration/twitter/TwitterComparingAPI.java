package integration.twitter;

import me.jhenrique.manager.TweetManager;
import me.jhenrique.manager.TwitterCriteria;
import me.jhenrique.model.Tweet;
import org.junit.BeforeClass;
import org.junit.Test;
import twitter.AdvancedSearchAPI;
import twitter.OfficialAPI;
import twitter.ScrollDownAPI;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class TwitterComparingAPI {

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
    public void crossTwitterAPICheck_OfficialAdvancedSearchAPI() throws TwitterException {
        OfficialAPI officialAPI = new OfficialAPI();
        List<Status> statuses = officialAPI.searchNumberTweetsByUser(50, "reuters", () -> twitter);
        List<Long> oauthIds =
                statuses.stream().map(Status::getId).sorted(Comparator.reverseOrder()).collect(Collectors.toList());

        AdvancedSearchAPI twitterSearch = new AdvancedSearchAPI();
        Supplier<TwitterCriteria> criteriaFunc = (TwitterCriteria::create);
        Function<TwitterCriteria, List<Tweet>> function = (TweetManager::getTweets);

        List<Tweet> tweets = twitterSearch.searchNumberTweetsByUser(50, "reuters", criteriaFunc, function);
        List<Long> searchIds =
                tweets.stream().map(tweet -> Long.valueOf(tweet.getId())).sorted(Comparator.reverseOrder())
                        .collect(Collectors.toList());

        // unofficial API 'Advanced Search' not contains all tweets
        oauthIds.removeAll(searchIds);
        assertThat(searchIds).isNotEmpty();
    }

    @Test
    public void crossTwitterAPICheck_OfficialScrollDownAPI() throws TwitterException, IOException {
        OfficialAPI officialAPI = new OfficialAPI();
        for (int i = 0; i < 10; i++) {
            List<Status> statuses = officialAPI.searchNumberTweetsByUser(200, "reuters", () -> twitter);
            List<Long> oauthIds =
                    statuses.stream().map(Status::getId).sorted(Comparator.reverseOrder()).collect(Collectors.toList());

            ScrollDownAPI scrollDownAPI = new ScrollDownAPI();
            List<ScrollDownAPI.Tweet> scrollDownTweets = scrollDownAPI.searchNumberTweetsByUser(200, "reuters");
            List<Long> scrollDownIds =
                    scrollDownTweets.stream().map(ScrollDownAPI.Tweet::getId).sorted(Comparator.reverseOrder())
                            .collect(Collectors.toList());

            // unofficial API 'Scroll Down' erratically not contains at least 1 tweet after the first 'scroll down', between the firsts 5 and 50
            List<Long> copyOauthIds = new ArrayList<>(oauthIds);
            copyOauthIds.removeAll(scrollDownIds);
            assertThat(copyOauthIds.size()).isGreaterThanOrEqualTo(1);
            assertThat(oauthIds.indexOf(copyOauthIds.get(0))).isBetween(5, 50);
        }
    }
}
