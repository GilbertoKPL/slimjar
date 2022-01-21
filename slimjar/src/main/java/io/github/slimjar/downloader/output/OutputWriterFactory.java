package io.github.slimjar.downloader.output;


import io.github.slimjar.downloader.strategy.FilePathStrategy;
import io.github.slimjar.resolver.data.Dependency;

public interface OutputWriterFactory {
    OutputWriter create(final Dependency param);

    FilePathStrategy getStrategy();
}
