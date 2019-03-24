package unit.twitter;

import me.jhenrique.manager.TwitterCriteria;
import me.jhenrique.model.Tweet;
import org.junit.Test;
import org.mockito.Mockito;
import twitter.AdvancedSearchAPI;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class AdvancedSearchAPITest {

    @Test
    public void searchNumberTweetsByUser_NoTweets() {

        TwitterCriteria twitterCriteria = Mockito.mock(TwitterCriteria.class);
        when(twitterCriteria.setUsername(anyString())).thenReturn(twitterCriteria);
        when(twitterCriteria.setMaxTweets(anyInt())).thenReturn(twitterCriteria);

        @SuppressWarnings("unchecked")
        Supplier<TwitterCriteria> criteriaFunc = (Supplier<TwitterCriteria>) Mockito.mock(Supplier.class);
        when(criteriaFunc.get())
                .thenReturn(twitterCriteria); //  Supplier<TwitterCriteria> criteriaFunc =  () -> twitterCriteria;

        Function<TwitterCriteria, List<Tweet>> getTweetsFunc = ((criteria) -> Collections.emptyList());

        AdvancedSearchAPI twitterSearch = new AdvancedSearchAPI();
        assertThat(twitterSearch.searchNumberTweetsByUser(1, "reuters", criteriaFunc, getTweetsFunc)).hasSize(0);

        verify(criteriaFunc, times(1)).get();
    }
}