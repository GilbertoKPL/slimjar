package github.slimjar.resolver;

import github.slimjar.resolver.data.Repository;
import github.slimjar.resolver.pinger.URLPinger;
import github.slimjar.resolver.enquirer.RepositoryEnquirerFactory;

import java.util.Collection;
import java.util.Map;

public final class CachingDependencyResolverFactory implements DependencyResolverFactory {
    private final URLPinger urlPinger;

    public CachingDependencyResolverFactory(final URLPinger urlPinger) {
        this.urlPinger = urlPinger;
    }

    @Override
    public DependencyResolver create(final Collection<Repository> repositories, final Map<String, ResolutionResult> preResolvedResults, final RepositoryEnquirerFactory enquirerFactory) {
        return new CachingDependencyResolver(urlPinger, repositories, enquirerFactory, preResolvedResults);
    }
}
