package pl.mjedynak;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.StringContains.containsString;

public class AppFunctionalTest {

    private static final String LOG_FILE = "application.log";
    private static final String LOG_MESSAGE = "request for resource";

    @Before
    public void setUp() throws Exception {
        startApplication();
    }


    private void startApplication() throws InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Runnable startApplicationTask = new Runnable() {
            public void run() {
                try {
                    String[] noArgs = null;
                    App.main(noArgs);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        executor.submit(startApplicationTask);

        // give some time for application to start
        Thread.sleep(3000);
    }

    @Test
    public void shouldLogInformationAboutRequest() throws IOException {
        // given
        Client client = Client.create();
        WebResource resource = client.resource("http://localhost:9998/resource");
        // when
        String result = resource.get(String.class);
        // then
        assertThat(result, is(notNullValue()));
        verifyLogMessage();
    }

    private void verifyLogMessage() throws IOException {
        Path logFile = Paths.get(LOG_FILE);
        try (BufferedReader reader = Files.newBufferedReader(logFile, Charset.defaultCharset())) {
            String log = reader.readLine();
            assertThat(log, containsString(LOG_MESSAGE));
        }
    }

}
