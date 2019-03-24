package unit.server;

import main.MainServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.Socket;

import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;

public class MainServerTest {

    // Given a TimelineAPI_NotToUse username and a number N, returns a JSON response with user’s last N tweets in uppercase and a final exclamation mark.
    // For example, a tweet like “Hello this is my first Tweet” would be converted to “HELLO THIS IS MY FIRST TWEET!”.

    @BeforeClass
    public static void setUp() {
        MainServer.start();
    }

    @AfterClass
    public static void tearDown() {
        MainServer.stop();
    }

    @Test
    public void main() {
        try (Socket socket = new Socket(MainServer.HOST, MainServer.PORT)) {
            assertThat(socket.getPort()).isEqualTo(MainServer.PORT);
            assertThat(socket.isConnected()).isTrue();
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

}