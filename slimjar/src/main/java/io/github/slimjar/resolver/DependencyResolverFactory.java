package io.github.slimjar.resolver;

import io.github.slimjar.resolver.data.Repository;
import io.github.slimjar.resolver.enquirer.RepositoryEnquirerFactory;

import java.util.Collection;
import java.util.Map;

@FunctionalInterface
public interface DependencyResolverFactory {
    DependencyResolver create(final Collection<Repository> repositories, final Map<String, ResolutionResult> preResolvedResults, final RepositoryEnquirerFactory enquirerFactory);
}
