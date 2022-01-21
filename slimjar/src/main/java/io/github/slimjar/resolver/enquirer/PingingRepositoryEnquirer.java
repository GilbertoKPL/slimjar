package io.github.slimjar.resolver.enquirer;

import io.github.slimjar.logging.LogDispatcher;
import io.github.slimjar.logging.ProcessLogger;
import io.github.slimjar.resolver.ResolutionResult;
import io.github.slimjar.resolver.data.Dependency;
import io.github.slimjar.resolver.data.Repository;
import io.github.slimjar.resolver.pinger.URLPinger;
import io.github.slimjar.resolver.strategy.PathResolutionStrategy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

public final class PingingRepositoryEnquirer implements RepositoryEnquirer {
    private static final ProcessLogger LOGGER = LogDispatcher.getMediatingLogger();
    private final Repository repository;
    private final PathResolutionStrategy dependencyURLCreationStrategy;
    private final PathResolutionStrategy checksumURLCreationStrategy;
    private final PathResolutionStrategy pomURLCreationStrategy;
    private final URLPinger urlPinger;

    public PingingRepositoryEnquirer(final Repository repository, final PathResolutionStrategy urlCreationStrategy, final PathResolutionStrategy checksumURLCreationStrategy, final PathResolutionStrategy pomURLCreationStrategy, final URLPinger urlPinger) {
        this.repository = repository;
        this.dependencyURLCreationStrategy = urlCreationStrategy;
        this.checksumURLCreationStrategy = checksumURLCreationStrategy;
        this.pomURLCreationStrategy = pomURLCreationStrategy;
        this.urlPinger = urlPinger;
    }

    @Override
    public ResolutionResult enquire(final Dependency dependency) {
        LOGGER.debug("Enquiring repositories to find {0}", dependency.getArtifactId());
        final Optional<URL> resolvedDependency = dependencyURLCreationStrategy.pathTo(repository, dependency)
                .stream().map((path) -> {
                    try {
                        return new URL(path);
                    } catch (MalformedURLException e) {
                        return null;
                    }
                }).filter(urlPinger::ping)
                .findFirst();
        if (!resolvedDependency.isPresent()) {
            return pomURLCreationStrategy.pathTo(repository, dependency).stream().map((path) -> {
                        try {
                            return new URL(path);
                        } catch (MalformedURLException e) {
                            return null;
                        }
                    }).filter(urlPinger::ping)
                    .findFirst()
                    .map(url -> new ResolutionResult(repository, null, null, true))
                    .orElse(null);
        }
        final Optional<URL> resolvedChecksum = checksumURLCreationStrategy.pathTo(repository, dependency)
                .stream().map((path) -> {
                    try {
                        return new URL(path);
                    } catch (MalformedURLException e) {
                        return null;
                    }
                }).filter(urlPinger::ping)
                .findFirst();
        return new ResolutionResult(repository, resolvedDependency.get(), resolvedChecksum.orElse(null), false);
    }
}
