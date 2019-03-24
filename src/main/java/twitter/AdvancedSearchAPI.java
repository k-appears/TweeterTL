package twitter;

import me.jhenrique.manager.TwitterCriteria;
import me.jhenrique.model.Tweet;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Scrapping tweet from non-official API
 *
 * API found at https://twitter.com/search-advanced
 *
 * PROS: has no limitation in number of tweets
 * CONS: not all tweets appear, see intgration test {@see integration.twitter.TwitterAPI#crossTwitterAPICheck_OfficialAdvancedSearchAPI()}
 * https://twitter.com/search?l=&q=from%3Areuters vs https://twitter.com/reuters
 */
public class AdvancedSearchAPI {

    /**
     * @param numberTweets     to search
     * @param userName         twitter user
     * @param criteriaFunction dependency injection instead of TwitterCriteria.create()
     * @param function         dependency injection instead of TweetManager.getTweets(criteria)
     * @return tweets found
     */
    public List<Tweet> searchNumberTweetsByUser(int numberTweets, String userName,
            Supplier<TwitterCriteria> criteriaFunction, Function<TwitterCriteria, List<Tweet>> function) {
        if (numberTweets <= 0) {
            return Collections.emptyList();
        }
        if (userName == null || userName.isEmpty()) {
            return Collections.emptyList();
        }

        TwitterCriteria criteria = criteriaFunction.get().setUsername(userName).setMaxTweets(numberTweets);
        return function.apply(criteria);
    }
}
