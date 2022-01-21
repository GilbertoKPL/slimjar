package github.slimjar.downloader;

import github.slimjar.downloader.output.OutputWriterFactory;
import github.slimjar.downloader.verify.DependencyVerifier;
import github.slimjar.resolver.DependencyResolver;

@FunctionalInterface
public interface DependencyDownloaderFactory {
    DependencyDownloader create(final OutputWriterFactory outputWriterFactory, final DependencyResolver resolver, final DependencyVerifier verifier);
}
