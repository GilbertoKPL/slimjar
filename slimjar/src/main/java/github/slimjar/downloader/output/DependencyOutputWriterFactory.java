package github.slimjar.downloader.output;

import github.slimjar.downloader.strategy.FilePathStrategy;
import github.slimjar.resolver.data.Dependency;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DependencyOutputWriterFactory implements OutputWriterFactory {
    private static final Logger LOGGER = Logger.getLogger(DependencyOutputWriterFactory.class.getName());
    private final FilePathStrategy outputFilePathStrategy;

    public DependencyOutputWriterFactory(final FilePathStrategy filePathStrategy) {
        this.outputFilePathStrategy = filePathStrategy;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public OutputWriter create(final Dependency dependency) {
        LOGGER.log(Level.FINEST, "Creating OutputWriter for {0}", dependency.getArtifactId());
        final File outputFile = outputFilePathStrategy.selectFileFor(dependency);
        outputFile.getParentFile().mkdirs();
        return new ChanneledFileOutputWriter(outputFile);
    }

    @Override
    public FilePathStrategy getStrategy() {
        return outputFilePathStrategy;
    }
}
