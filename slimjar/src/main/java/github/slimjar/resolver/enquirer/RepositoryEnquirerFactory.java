package github.slimjar.resolver.enquirer;

import github.slimjar.resolver.data.Repository;

@FunctionalInterface
public interface RepositoryEnquirerFactory {
    RepositoryEnquirer create(final Repository repository);
}
