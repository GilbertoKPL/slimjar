package github.slimjar.downloader;

import github.slimjar.downloader.verify.DependencyVerifier;
import github.slimjar.resolver.DependencyResolver;
import github.slimjar.downloader.output.OutputWriterFactory;

public final class URLDependencyDownloaderFactory implements DependencyDownloaderFactory {

    @Override
    public DependencyDownloader create(final OutputWriterFactory outputWriterFactory, final DependencyResolver resolver, final DependencyVerifier verifier) {
        return new URLDependencyDownloader(outputWriterFactory, resolver, verifier);
    }
}
