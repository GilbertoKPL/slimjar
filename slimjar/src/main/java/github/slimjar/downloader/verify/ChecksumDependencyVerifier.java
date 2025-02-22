package github.slimjar.downloader.verify;

import github.slimjar.logging.LogDispatcher;
import github.slimjar.logging.ProcessLogger;
import github.slimjar.resolver.DependencyResolver;
import github.slimjar.resolver.ResolutionResult;
import github.slimjar.resolver.data.Dependency;
import github.slimjar.util.Connections;
import github.slimjar.downloader.output.OutputWriter;
import github.slimjar.downloader.output.OutputWriterFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;

public final class ChecksumDependencyVerifier implements DependencyVerifier {
    private static final ProcessLogger LOGGER = LogDispatcher.getMediatingLogger();
    private final DependencyResolver resolver;
    private final OutputWriterFactory outputWriterFactory;
    private final DependencyVerifier fallbackVerifier;
    private final ChecksumCalculator checksumCalculator;

    public ChecksumDependencyVerifier(final DependencyResolver resolver, final OutputWriterFactory outputWriterFactory, final DependencyVerifier fallbackVerifier, final ChecksumCalculator checksumCalculator) {
        this.resolver = resolver;
        this.outputWriterFactory = outputWriterFactory;
        this.fallbackVerifier = fallbackVerifier;
        this.checksumCalculator = checksumCalculator;
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public boolean verify(final File file, final Dependency dependency) throws IOException {
        if (!file.exists()) return false;
        LOGGER.debug("Verifying checksum for {0}", dependency.getArtifactId());
        final File checksumFile = outputWriterFactory.getStrategy().selectFileFor(dependency);
        checksumFile.getParentFile().mkdirs();
        if (!checksumFile.exists() && !prepareChecksumFile(checksumFile, dependency)) {
            LOGGER.log("Unable to resolve checksum for {0}, falling back to fallbackVerifier!", dependency.getArtifactId());
            return fallbackVerifier.verify(file, dependency);
        }
        if (checksumFile.length() == 0L) {
            LOGGER.log("Required checksum not found for {0}, using fallbackVerifier!", dependency.getArtifactId());
            return fallbackVerifier.verify(file, dependency);
        }
        final String actualChecksum = checksumCalculator.calculate(file);
        final String expectedChecksum = new String(Files.readAllBytes(checksumFile.toPath())).trim();
        LOGGER.debug("{0} -> Actual checksum: {1};", dependency.getArtifactId(), actualChecksum);
        LOGGER.debug("{0} -> Expected checksum: {1};", dependency.getArtifactId(), expectedChecksum);
        final boolean match = Objects.equals(actualChecksum, expectedChecksum);
        LOGGER.debug("Checksum {0} for {1}", match ? "matched" : "match failed", dependency.getArtifactId());
        return Objects.equals(actualChecksum, expectedChecksum);
    }

    @Override
    public File getChecksumFile(final Dependency dependency) {
        final File checksumFile = outputWriterFactory.getStrategy().selectFileFor(dependency);
        checksumFile.getParentFile().mkdirs();
        return checksumFile;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private boolean prepareChecksumFile(final File checksumFile, final Dependency dependency) throws IOException {
        final Optional<ResolutionResult> result = resolver.resolve(dependency);
        if (!result.isPresent()) {
            return false;
        } else {
            final URL checkSumUrl = result.get().getChecksumURL();
            LOGGER.debug("Resolved checksum URL for {0} as {1}", dependency.getArtifactId(), checkSumUrl);
            if (checkSumUrl == null) {
                checksumFile.createNewFile();
                return true;
            }
            final URLConnection connection = Connections.createDownloadConnection(checkSumUrl);
            final InputStream inputStream = connection.getInputStream();
            final OutputWriter outputWriter = outputWriterFactory.create(dependency);
            outputWriter.writeFrom(inputStream, connection.getContentLength());
            Connections.tryDisconnect(connection);
            LOGGER.debug("Downloaded checksum for {0}", dependency.getArtifactId());
        }
        return true;
    }
}
