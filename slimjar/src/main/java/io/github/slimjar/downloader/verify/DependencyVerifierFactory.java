package io.github.slimjar.downloader.verify;

import io.github.slimjar.resolver.DependencyResolver;

public interface DependencyVerifierFactory {
    DependencyVerifier create(final DependencyResolver resolver);
}
