package github.slimjar.resolver;

import github.slimjar.resolver.data.Repository;
import github.slimjar.resolver.enquirer.RepositoryEnquirerFactory;

import java.util.Collection;
import java.util.Map;

@FunctionalInterface
public interface DependencyResolverFactory {
    DependencyResolver create(final Collection<Repository> repositories, final Map<String, ResolutionResult> preResolvedResults, final RepositoryEnquirerFactory enquirerFactory);
}
