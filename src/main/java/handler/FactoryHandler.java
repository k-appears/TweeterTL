package handler;

import org.apache.log4j.Logger;
import twitter.OfficialAPI;

import java.util.List;
import java.util.Map;

/**
 * Factory class which decides which handle to create from the path of the request.
 */
public final class FactoryHandler {

    private static Logger logger = Logger.getLogger(FactoryHandler.class);

    private static final String GET = "GET";
    private static final int MBYTE_PER_BYTE = (1024 * 1024);
    private static final String TWEETER = "tweeter";

    public static final String ENDPOINT = "/" + TWEETER;
    public static final String PROTOCOL = "http";
    public static final String USERNAME = "username";
    public static final String NUM_TWEETS = "num_tweets";

    public static Handler create(final List<String> segments, Map<String, List<String>> queryParameters,
            final String requestBody, final String requestMethod) {
        if (!isMemoryAvailable()) {
            return new SystemUnavailableRequestHandler();
        }
        if (segments.size() == 1) {
            String action = segments.get(0);
            if (TWEETER.equals(action)) {
                if (requestMethod.equals(GET) && areParametersCorrectForTweeter(queryParameters) && requestBody
                        .isEmpty()) {
                    String username = queryParameters.get(USERNAME).get(0);
                    int numTweets = Integer.valueOf(queryParameters.get(NUM_TWEETS).get(0));
                    return new TweeterHandler(username, numTweets, new OfficialAPI());
                }
            } else {
                return new BadRequestHandler();
            }

        }
        return new BadRequestHandler();
    }

    private static boolean areParametersCorrectForTweeter(Map<String, List<String>> queryParameters) {
        return queryParameters.size() == 2 && queryParameters.keySet().contains(NUM_TWEETS)
                && queryParameters.get(NUM_TWEETS).size() == 1 && queryParameters.get(NUM_TWEETS).get(0).matches("\\d+")
                && queryParameters.keySet().contains(USERNAME) && queryParameters.get(USERNAME).size() == 1
                && queryParameters.get(USERNAME).size()
                <= 50; // account name no more than 50 chars https://help.twitter.com/es/managing-your-account/twitter-username-rules
    }

    /**
     * When cache mechanism is implemented, the system could be memory overflown, this method will avoid it.
     *
     * @return if there is memory available in the system
     */
    private static boolean isMemoryAvailable() {
        long totalMemoryInUse = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();
        long memoryUsed = totalMemoryInUse - freeMemory;
        double memoryUsedPercentage = ((memoryUsed * 1.0) / totalMemoryInUse) * 100;

        //Threshold for maximum utilization of the JVM Ram available, {@link #isMemoryAvailable()}.
        double MEMORY_PERCENTAGE_ALLOWED = 90;
        if (memoryUsedPercentage > MEMORY_PERCENTAGE_ALLOWED) {
            logger.info("Memory threshold exceeded:");
            long maxMemory = Runtime.getRuntime().maxMemory();
            logger.info("Maximum memory JVM will attempt to use (MB): " + (maxMemory == Long.MAX_VALUE ?
                    "no limit" :
                    maxMemory / MBYTE_PER_BYTE));
            logger.info("Amount of free memory available to the JVM (MB): " + freeMemory / MBYTE_PER_BYTE);
            logger.info("Total memory currently in use by the JVM (MB): " + totalMemoryInUse / MBYTE_PER_BYTE);
            logger.info("Memory use " + Math.floor(memoryUsedPercentage * 100) / 100 + " %");
            logger.info("Memory threshold " + MEMORY_PERCENTAGE_ALLOWED + " %");
            return false;
        }
        return true;
    }

}
