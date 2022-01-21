package github.slimjar.resolver.reader.dependency;


import github.slimjar.resolver.data.DependencyData;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public final class URLDependencyDataProvider implements DependencyDataProvider {
    private final DependencyReader dependencyReader;
    private final URL depFileURL;
    private DependencyData cachedData = null;

    public URLDependencyDataProvider(final DependencyReader dependencyReader, final URL depFileURL) {
        this.dependencyReader = dependencyReader;
        this.depFileURL = depFileURL;
    }

    public DependencyReader getDependencyReader() {
        return dependencyReader;
    }

    @Override
    public DependencyData get() throws IOException, ReflectiveOperationException {
        if (cachedData != null) {
            return cachedData;
        }
        try (InputStream is = depFileURL.openStream()) {
            cachedData = dependencyReader.read(is);
            return cachedData;
        }
    }
}
