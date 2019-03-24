package integration.twitter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import twitter.BrokenAPI_NotToUse;
import uk.co.tomkdickinson.twitter.search.Tweet;
import uk.co.tomkdickinson.twitter.search.TwitterResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Deprecated
@Ignore
public class TwitterAPIBrokenTest {

    @Test
    public void searchNumberTweetsEmpty() {
        BrokenAPI_NotToUse twitterAPIBroken = new BrokenAPI_NotToUse();
        TwitterResponse TwitterResponse = Mockito.mock(TwitterResponse.class);
        when(TwitterResponse.getTweets()).thenReturn(new ArrayList<>());
        Function<URL, TwitterResponse> function = ((url) -> TwitterResponse);

        List<Tweet> tweets = twitterAPIBroken.searchNumberTweets(5, "LetGo", function);

        assertThat(tweets).isEmpty();

    }

    @Test
    public void searchNumberTweets() {
        BrokenAPI_NotToUse twitterAPIBroken = new BrokenAPI_NotToUse();
        Function<URL, TwitterResponse> function = getResponseFunction();

        List<Tweet> tweets = twitterAPIBroken.searchNumberTweets(5, "LetGo", function);

        assertThat(tweets).isNotEmpty();

    }

    private Function<URL, TwitterResponse> getResponseFunction() {
        return url -> {
            BufferedReader reader;
            try {
                reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String json = reader.lines().collect(Collectors.joining());
            Gson gson = new Gson();
            TwitterResponse TwitterResponse = gson.fromJson(json, TwitterResponse.class);


            JsonObject jsonResponse = (JsonObject) new JsonParser().parse(json);
            String items_html = jsonResponse.getAsJsonObject("items_html").getAsString();
            TwitterResponse.setItems_html(items_html);
            return TwitterResponse;
        };
    }
}