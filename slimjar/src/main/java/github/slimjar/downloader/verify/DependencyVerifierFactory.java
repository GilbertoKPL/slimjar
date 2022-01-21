package github.slimjar.downloader.verify;

import github.slimjar.resolver.DependencyResolver;

public interface DependencyVerifierFactory {
    DependencyVerifier create(final DependencyResolver resolver);
}
