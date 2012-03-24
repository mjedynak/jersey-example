package pl.mjedynak;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.jayway.awaitility.Awaitility.await;
import static com.sun.jersey.api.client.ClientResponse.Status.OK;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.StringContains.containsString;
import static pl.mjedynak.resource.Resource.RESOURCE;

public class AppFunctionalTest {

    private static final String BASE_URL = "http://localhost:9998";
    private static final String LOG_FILE = "application.log";
    private static final String LOG_MESSAGE = "request for resource";

    private Client client = Client.create();

    @Before
    public void setUp() throws Exception {
        startApplication();
    }


    private void startApplication() throws Exception {
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

        await().until(serverIsStarted());
    }

    private Callable<Boolean> serverIsStarted() {
        return new Callable<Boolean>() {
            public Boolean call() {
                boolean result = false;
                try {
                    WebResource resource = client.resource(BASE_URL + "/application.wadl");
                    ClientResponse head = resource.head();
                    if (responseIsOk(head)) {
                        result = true;
                    }
                } catch (ClientHandlerException e) {
                    // swallow exception
                }
                return result;
            }

            private boolean responseIsOk(ClientResponse head) {
                return head != null && head.getClientResponseStatus() == OK;
            }
        };

    }

    @Test
    public void shouldLogInformationAboutRequest() throws IOException {
        // given
        WebResource resource = client.resource(BASE_URL + "/resource");
        // when
        String result = resource.get(String.class);
        // then
        assertThat(result, is(RESOURCE));
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
