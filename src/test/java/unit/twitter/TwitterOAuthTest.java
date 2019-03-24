package unit.twitter;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import twitter.OfficialAPI;
import twitter4j.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

public class TwitterOAuthTest {

    @Mock private List<Status> responseList;

    @Test
    public void searchNumberTweetsByUser_NoTweets() throws TwitterException {
        Twitter twitter = Mockito.mock(Twitter.class);
        when(twitter.getUserTimeline(anyLong(), any(Paging.class))).thenReturn((ResponseList<Status>) responseList);
        OfficialAPI officialAPI = new OfficialAPI();
        assertThat(officialAPI.searchNumberTweetsByUser(0, "reuters", () -> twitter)).hasSize(0);

    }

}