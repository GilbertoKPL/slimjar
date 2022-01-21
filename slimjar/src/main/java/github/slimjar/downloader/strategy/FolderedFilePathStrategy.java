package github.slimjar.downloader.strategy;


import github.slimjar.resolver.data.Dependency;

import java.io.File;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class FolderedFilePathStrategy implements FilePathStrategy {
    private static final Logger LOGGER = Logger.getLogger(FolderedFilePathStrategy.class.getName());
    private static final String DEPENDENCY_FILE_FORMAT = "%s/%s/%s/%s/%3$s-%4$s.jar";
    private final File rootDirectory;


    private FolderedFilePathStrategy(File rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public static FilePathStrategy createStrategy(final File rootDirectory) throws IllegalArgumentException {
        if (!rootDirectory.exists()) {
            boolean created = rootDirectory.mkdirs();
            if (!created) {
                throw new IllegalArgumentException("Could not create specified directory: " + rootDirectory);
            }
        }
        if (!rootDirectory.isDirectory()) {
            throw new IllegalArgumentException("Expecting a directory for download root! " + rootDirectory);
        }
        return new FolderedFilePathStrategy(rootDirectory);
    }

    @Override
    public File selectFileFor(Dependency dependency) {
        final String extendedVersion = Optional.ofNullable(dependency.getSnapshotId()).map(s -> "-" + s).orElse("");
        final String path = String.format(
                DEPENDENCY_FILE_FORMAT,
                rootDirectory.getPath(),
                dependency.getGroupId().replace('.', '/'),
                dependency.getArtifactId(),
                dependency.getVersion() + extendedVersion
        );
        LOGGER.log(Level.FINEST, "Selected jar file for " + dependency.getArtifactId() + " at " + path);
        return new File(path);
    }
}
