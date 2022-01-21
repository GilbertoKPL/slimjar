package github.slimjar.resolver.pinger;

import github.slimjar.logging.LogDispatcher;
import github.slimjar.logging.ProcessLogger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

public final class HttpURLPinger implements URLPinger {
    private static final ProcessLogger LOGGER = LogDispatcher.getMediatingLogger();
    private static final String SLIMJAR_USER_AGENT = "SlimjarApplication/* URL Validation Ping";
    private static final Collection<String> SUPPORTED_PROTOCOLS = Arrays.asList("HTTP", "HTTPS");

    @Override
    public boolean ping(final URL url) {
        final String urlStr = url.toString();
        LOGGER.debug("Pinging {0}", urlStr);
        if (!isSupported(url)) {
            LOGGER.debug("Protocol not supported for {0}", url.toString());
            return false;
        }
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(1000 * 5);
            connection.addRequestProperty("User-Agent", SLIMJAR_USER_AGENT);
            connection.connect();
            final boolean result = connection.getResponseCode() == HttpURLConnection.HTTP_OK;
            LOGGER.debug("Ping {1} for {0}", url.toString(), result ? "successful" : "failed");
            return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            LOGGER.debug("Ping failed for {0}", url.toString());
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public boolean isSupported(final URL url) {
        final String protocol = url.getProtocol().toUpperCase(Locale.ENGLISH);
        return SUPPORTED_PROTOCOLS.contains(protocol);
    }
}
