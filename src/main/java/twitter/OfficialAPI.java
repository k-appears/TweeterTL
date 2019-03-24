package twitter;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.util.*;
import java.util.function.Supplier;

public class OfficialAPI implements TwitterAPI {

    // maximum in paging is 1000 https://developer.twitter.com/en/docs/ads/general/guides/pagination
    // but when it set to different than 200 it does not work
    private static final int COUNT = 200;

    public List<Status> searchNumberTweetsByUser(int numberTweets, String userName, Supplier<Twitter> twitterFunc)
            throws TwitterException {
        Twitter twitter = twitterFunc.get(); // Injection
        if (numberTweets <= 0) {
            return Collections.emptyList();
        }
        if (numberTweets > 3200) {
            throw new IllegalArgumentException("Number of tweets limited to 3200 using API v1.1 " + System.lineSeparator() +
                    "See https://developer.twitter.com/en/docs/tweets/timelines/api-reference/get-statuses-user_timeline.html");
        }
        if (userName == null || userName.isEmpty()) {
            return Collections.emptyList();
        }
        if (numberTweets < COUNT) {
            Paging paging = new Paging(1, numberTweets);
            return twitter.getUserTimeline(userName, paging);
        } else {

            Set<Status> accTweets = new HashSet<>(numberTweets);
            int numTweetsLeft = numberTweets;
            List<Status> queriedTweets;
            Paging paging = new Paging();
            paging.setCount(COUNT);
            while (numTweetsLeft > 0) {
                queriedTweets = twitter.getUserTimeline(userName, paging);
                accTweets.addAll(queriedTweets);
                numTweetsLeft = numberTweets - accTweets.size();
                long lastId = queriedTweets.get(queriedTweets.size() - 1).getId();
                paging.maxId(lastId);
            }
            // TODO for debugging
            // new ArrayList<>(accTweets).subList(0, numberTweets - 1).stream().sorted(Comparator.comparing(Status::getCreatedAt).reversed()).collect(Collectors
            return new ArrayList<>(accTweets).subList(0, numberTweets);
        }
    }

}
