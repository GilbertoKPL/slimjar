package io.github.slimjar.resolver.enquirer;

import io.github.slimjar.resolver.data.Repository;
import io.github.slimjar.resolver.pinger.URLPinger;
import io.github.slimjar.resolver.strategy.PathResolutionStrategy;

public final class PingingRepositoryEnquirerFactory implements RepositoryEnquirerFactory {
    private final PathResolutionStrategy pathResolutionStrategy;
    private final PathResolutionStrategy checksumURLCreationStrategy;
    private final PathResolutionStrategy pomURLCreationStrategy;
    private final URLPinger urlPinger;

    public PingingRepositoryEnquirerFactory(final PathResolutionStrategy pathResolutionStrategy, final PathResolutionStrategy checksumURLCreationStrategy, final PathResolutionStrategy pomURLCreationStrategy, final URLPinger urlPinger) {
        this.pathResolutionStrategy = pathResolutionStrategy;
        this.checksumURLCreationStrategy = checksumURLCreationStrategy;
        this.pomURLCreationStrategy = pomURLCreationStrategy;
        this.urlPinger = urlPinger;
    }

    public PathResolutionStrategy getPathResolutionStrategy() {
        return pathResolutionStrategy;
    }

    public PathResolutionStrategy getChecksumURLCreationStrategy() {
        return checksumURLCreationStrategy;
    }

    public PathResolutionStrategy getPomURLCreationStrategy() {
        return pomURLCreationStrategy;
    }

    @Override
    public RepositoryEnquirer create(final Repository repository) {
        return new PingingRepositoryEnquirer(repository, pathResolutionStrategy, checksumURLCreationStrategy, pomURLCreationStrategy, urlPinger);
    }
}
