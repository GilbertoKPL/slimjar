package io.github.slimjar.resolver.enquirer;

import io.github.slimjar.resolver.data.Repository;

@FunctionalInterface
public interface RepositoryEnquirerFactory {
    RepositoryEnquirer create(final Repository repository);
}
