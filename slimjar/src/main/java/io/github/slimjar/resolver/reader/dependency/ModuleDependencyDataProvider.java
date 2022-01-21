package io.github.slimjar.resolver.reader.dependency;

import io.github.slimjar.resolver.data.DependencyData;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public final class ModuleDependencyDataProvider implements DependencyDataProvider {
    private final DependencyReader dependencyReader;
    private final URL moduleUrl;

    public ModuleDependencyDataProvider(final DependencyReader dependencyReader, final URL moduleUrl) {
        this.dependencyReader = dependencyReader;
        this.moduleUrl = moduleUrl;
    }

    @Override
    public DependencyData get() throws IOException, ReflectiveOperationException {
        final URL depFileURL = new URL("jar:file:" + moduleUrl.getFile() + "!/plugin.json");

        final URLConnection connection = depFileURL.openConnection();
        if (!(connection instanceof JarURLConnection)) {
            throw new AssertionError("Invalid Module URL provided(Non-Jar File)");
        }
        final JarURLConnection jarURLConnection = (JarURLConnection) connection;
        final JarFile jarFile = jarURLConnection.getJarFile();
        final ZipEntry dependencyFileEntry = jarFile.getEntry("plugin.json");
        if (dependencyFileEntry == null) {
            return new DependencyData(
                    Collections.emptySet(),
                    Collections.emptySet(),
                    Collections.emptySet(),
                    Collections.emptySet()
            );
        }

        try (InputStream inputStream = jarFile.getInputStream(dependencyFileEntry)) {
            return dependencyReader.read(inputStream);
        }
    }
}
