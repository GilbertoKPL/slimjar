package io.github.slimjar.downloader.strategy;

import io.github.slimjar.resolver.data.Dependency;

import java.io.File;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class RelocationFilePathStrategy implements FilePathStrategy {
    private static final Logger LOGGER = Logger.getLogger(FolderedFilePathStrategy.class.getName());
    private static final String DEPENDENCY_FILE_FORMAT = "%s/%s/%s/%s/relocated/%5$s/%3$s-%4$s.jar";
    private final File rootDirectory;
    private final String applicationName;


    private RelocationFilePathStrategy(final String applicationName, final File rootDirectory) {
        this.rootDirectory = rootDirectory;
        this.applicationName = applicationName;
    }

    public static FilePathStrategy createStrategy(final File rootDirectory, final String applicationName) throws IllegalArgumentException {
        if (!rootDirectory.exists()) {
            boolean created = rootDirectory.mkdirs();
            if (!created) {
                throw new IllegalArgumentException("Could not create specified directory: " + rootDirectory);
            }
        }
        if (!rootDirectory.isDirectory()) {
            throw new IllegalArgumentException("Expecting a directory for download root! " + rootDirectory);
        }
        return new RelocationFilePathStrategy(applicationName, rootDirectory);
    }

    @Override
    public File selectFileFor(final Dependency dependency) {
        final String extendedVersion = Optional.ofNullable(dependency.getSnapshotId()).map(s -> "-" + s).orElse("");
        final String path = String.format(
                DEPENDENCY_FILE_FORMAT,
                rootDirectory.getPath(),
                dependency.getGroupId().replace('.', '/'),
                dependency.getArtifactId(),
                dependency.getVersion() + extendedVersion,
                applicationName
        );
        LOGGER.log(Level.FINEST, "Selected file for relocated " + dependency.getArtifactId() + " at " + path);
        return new File(path);
    }
}
