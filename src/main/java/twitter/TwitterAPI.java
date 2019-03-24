package twitter;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.util.List;
import java.util.function.Supplier;

public interface TwitterAPI {

    List<Status> searchNumberTweetsByUser(int numberTweets, String userName, Supplier<Twitter> twitterFunc)
            throws TwitterException;
}
