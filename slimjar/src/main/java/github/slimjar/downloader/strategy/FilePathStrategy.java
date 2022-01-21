package github.slimjar.downloader.strategy;

import github.slimjar.resolver.data.Dependency;

import java.io.File;

public interface FilePathStrategy {
    static FilePathStrategy createDefault(final File root) {
        return FolderedFilePathStrategy.createStrategy(root);
    }

    static FilePathStrategy createRelocationStrategy(final File root, final String applicationName) {
        return RelocationFilePathStrategy.createStrategy(root, applicationName);
    }

    File selectFileFor(final Dependency dependency);
}
