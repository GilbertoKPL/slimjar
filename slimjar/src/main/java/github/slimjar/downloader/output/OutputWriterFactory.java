package github.slimjar.downloader.output;


import github.slimjar.downloader.strategy.FilePathStrategy;
import github.slimjar.resolver.data.Dependency;

public interface OutputWriterFactory {
    OutputWriter create(final Dependency param);

    FilePathStrategy getStrategy();
}
