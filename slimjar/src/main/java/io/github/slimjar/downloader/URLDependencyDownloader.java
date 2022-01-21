package io.github.slimjar.downloader;

import io.github.slimjar.downloader.output.OutputWriter;
import io.github.slimjar.downloader.output.OutputWriterFactory;
import io.github.slimjar.downloader.verify.DependencyVerifier;
import io.github.slimjar.logging.LogDispatcher;
import io.github.slimjar.logging.ProcessLogger;
import io.github.slimjar.resolver.DependencyResolver;
import io.github.slimjar.resolver.ResolutionResult;
import io.github.slimjar.resolver.UnresolvedDependencyException;
import io.github.slimjar.resolver.data.Dependency;
import io.github.slimjar.util.Connections;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Arrays;

public final class URLDependencyDownloader implements DependencyDownloader {
    private static final byte[] BOM_BYTES = "bom-file".getBytes();
    private static final ProcessLogger LOGGER = LogDispatcher.getMediatingLogger();
    private final OutputWriterFactory outputWriterProducer;
    private final DependencyResolver dependencyResolver;
    private final DependencyVerifier verifier;

    public URLDependencyDownloader(final OutputWriterFactory outputWriterProducer, DependencyResolver dependencyResolver, DependencyVerifier verifier) {
        this.outputWriterProducer = outputWriterProducer;
        this.dependencyResolver = dependencyResolver;
        this.verifier = verifier;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public File download(final Dependency dependency) throws IOException {
        final File expectedOutputFile = outputWriterProducer.getStrategy().selectFileFor(dependency);
        if (expectedOutputFile.exists()
                && expectedOutputFile.length() == BOM_BYTES.length
                && Arrays.equals(Files.readAllBytes(expectedOutputFile.toPath()), BOM_BYTES)
        ) {
            return null;
        }
        if (verifier.verify(expectedOutputFile, dependency)) {
            return expectedOutputFile;
        }

        final ResolutionResult result = dependencyResolver.resolve(dependency)
                .orElseThrow(() -> new UnresolvedDependencyException(dependency));

        if (result.isAggregator()) {
            expectedOutputFile.getParentFile().mkdirs();
            expectedOutputFile.createNewFile();
            Files.write(expectedOutputFile.toPath(), BOM_BYTES);
            return null;
        }

        expectedOutputFile.delete();
        final File checksumFile = verifier.getChecksumFile(dependency);
        if (checksumFile != null) {
            checksumFile.delete();
        }

        LOGGER.log("Downloading {0}:{1}:{2}...", dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());

        final URL url = result.getDependencyURL();
        LOGGER.debug("Connecting to {0}", url);
        final URLConnection connection = Connections.createDownloadConnection(url);
        final InputStream inputStream = connection.getInputStream();
        LOGGER.debug("Connection successful! Downloading {0}", dependency.getArtifactId() + "...");
        final OutputWriter outputWriter = outputWriterProducer.create(dependency);
        LOGGER.debug("{0}.Size = {1}", dependency.getArtifactId(), connection.getContentLength());
        final File downloadResult = outputWriter.writeFrom(inputStream, connection.getContentLength());
        Connections.tryDisconnect(connection);
        verifier.verify(downloadResult, dependency);
        LOGGER.debug("Artifact {0} downloaded successfully!", dependency.getArtifactId());

        LOGGER.debug("Downloaded {0} successfully!", dependency.getArtifactId());
        return downloadResult;
    }
}
