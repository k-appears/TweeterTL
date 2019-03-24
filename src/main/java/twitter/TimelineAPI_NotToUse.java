package twitter;

import org.apache.http.client.utils.URIBuilder;
import uk.co.tomkdickinson.twitter.search.InvalidQueryException;
import uk.co.tomkdickinson.twitter.search.Tweet;
import uk.co.tomkdickinson.twitter.search.TwitterResponse;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Investigation to check project https://github.com/tomkdickinson/TwitterSearchAPI
 *
 * Not to use because the method @see uk.co.tomkdickinson.twitter.search.TwitterResponse#getTweets returns empty tweets
 */
@Deprecated
public class TimelineAPI_NotToUse {

    private final static String QUERY_FROM_PARAM = "q";

    private final static String SCROLL_CURSOR_PARAM = "max_position";
    private final static String TWITTER_SEARCH_URL = "https://twitter.com/i/search/timeline";
    // https://twitter.com/i/search/timeline?&q=from:LetGo&f=tweets

    public List<Tweet> searchNumberTweets(int numberTweets, String userName, Function<URL, TwitterResponse> searchFunction)
            throws InvalidQueryException {
        if (userName == null || userName.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            URL url = createTimelineURL(Optional.empty(), userName);
            TwitterResponse twitterResponseCustom = searchFunction.apply(url); //AdvancedSearchAPI.executeSearch(url);
            if (twitterResponseCustom == null) {
                throw new InvalidQueryException("Can not connect to twitter");
            }

            List<Tweet> queriedTweets = twitterResponseCustom.getTweets();
            if (queriedTweets.isEmpty()) {
                return Collections.emptyList();
            } else if (queriedTweets.size() == 1) {
                return Collections.singletonList(queriedTweets.get(0));
            } else {

                List<Tweet> foundTweets = new ArrayList<>();
                while (foundTweets.size() < numberTweets) {

                    if (queriedTweets.size() + foundTweets.size() > numberTweets) {
                        int firstNumberTweets = numberTweets - foundTweets.size();
                        foundTweets.addAll(queriedTweets.subList(0, firstNumberTweets));
                        return foundTweets;
                    } else {

                        foundTweets.addAll(queriedTweets);

                        // query tweeter again to get older tweets
                        String minTweet = queriedTweets.get(0).getId();
                        String maxTweet = queriedTweets.get(queriedTweets.size() - 1).getId();
                        if (!minTweet.equals(maxTweet)) {
                            String maxPosition = "TWEET-" + maxTweet + "-" + minTweet;
                            url = createTimelineURL(Optional.of(maxPosition), userName);
                            twitterResponseCustom = searchFunction.apply(url); //AdvancedSearchAPI.executeSearch(url);
                            queriedTweets = twitterResponseCustom.getTweets();
                        } else {
                            // Not more tweets
                            return foundTweets;
                        }
                    }
                }
            }
        } catch (URISyntaxException | MalformedURLException e) {
            throw new InvalidQueryException("Can not connect to twitter: " + e);
        }
        return Collections.emptyList();
    }

    private static URL createTimelineURL(Optional<String> maxPosition, String userName)
            throws URISyntaxException, MalformedURLException {
        URIBuilder uriBuilder;
        uriBuilder = new URIBuilder(TWITTER_SEARCH_URL);

        uriBuilder.addParameter(QUERY_FROM_PARAM, "from:" + userName);
        uriBuilder.addParameter("f", "tweets");

        maxPosition.ifPresent(maxPos -> uriBuilder.addParameter(SCROLL_CURSOR_PARAM, maxPos));


        return uriBuilder.build().toURL();

    }
}
