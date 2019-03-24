package twitter;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Scrapping tweet from non-official API
 * Same as clicking on "Showing more" button inside a tweeter account
 *
 * CONS: limitation to 830-850 tweets, see integration tests
 * Some tweets are missed, since 'max_position' is not working correctly
 *
 * PROS: None
 */
public class ScrollDownAPI {

    private static final HttpClient defaultHttpClient = HttpClients.createDefault();

    /**
     * Example    <code>https://syndication.twitter.com/timeline/profile?callback=__twttrf.callback&dnt=false&screen_name=TwitterDev&suppress_response_codes=true&max_position=982346373969330177</code>
     * using max_pos to get the latest tweets
     *
     * @param numberTweets to retreive
     * @param userName     account name
     * @return list of tweets for the account <pre>userName</pre>
     * @throws IOException when url malformed
     */
    public List<Tweet> searchNumberTweetsByUser(int numberTweets, String userName) throws IOException {
        if (numberTweets <= 0) {
            return new ArrayList<>();
        }
        List<Tweet> foundTweets = new ArrayList<>(numberTweets);
        String refreshCursor = "";

        JSONObject json = new JSONObject(getURLResponse(userName, refreshCursor));
        if (json.getJSONObject("headers").getInt("status") == 404) {
            throw new IllegalArgumentException(
                    "Error to twitter: " + json.getJSONObject("headers").getString("message"));
        }

        while (foundTweets.size() < numberTweets) {
            refreshCursor = json.getJSONObject("headers").getString("minPosition");
            Document doc = Jsoup.parse((String) json.get("body"));
            Elements tweets = doc.select(".timeline-Tweet");
            if (tweets.size() == 0) {
                return new ArrayList<>(foundTweets);
            }
            Stream<Tweet> nextTweets = tweets.stream().map(tweet -> {
                String txt = tweet.select("p.timeline-Tweet-text").text().replaceAll("[^\\u0000-\\uFFFF]", "");
                String dateString = tweet.select("time").attr("datetime");
                String pattern = "yyyy-MM-dd'T'HH:mm:ssZ";
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
                LocalDateTime dateTime = LocalDateTime.parse(dateString, dtf);
                long id = Long.valueOf(tweet.attr("data-tweet-id"));
                return new Tweet(id, "https://twitter.com/" + userName + "/status/" + id, userName, txt, dateTime);
            });

            foundTweets.addAll(nextTweets.collect(Collectors.toList()));
            json = new JSONObject(getURLResponse(userName, refreshCursor));
        }
        return foundTweets.subList(0, numberTweets);
    }

    private static String getURLResponse(String username, String scrollCursor) throws IOException {
        String max_position = scrollCursor != null ? "&max_position=" + scrollCursor : "";
        String url = String.format("https://syndication.twitter.com/timeline/profile?callback=__twttrf.callback&"
                        + "dnt=false&screen_name=%s&suppress_response_codes=true%s", URLEncoder.encode(username, "UTF-8"),
                max_position);
        HttpGet httpGet = new HttpGet(url);
        HttpEntity resp = defaultHttpClient.execute(httpGet).getEntity();

        String result = EntityUtils.toString(resp);
        return result.substring("/**/__twttrf.callback(".length(), result.length() - 2);
    }

    public class Tweet {

        private final long id;
        private final String permalink;
        private final String username;
        private final String text;
        private final LocalDateTime date;

        public Tweet(long id, String permalink, String username, String text, LocalDateTime date) {
            this.id = id;
            this.permalink = permalink;
            this.username = username;
            this.text = text;
            this.date = date;
        }

        public long getId() {
            return this.id;
        }
    }

}
