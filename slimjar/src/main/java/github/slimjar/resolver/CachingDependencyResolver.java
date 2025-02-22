package github.slimjar.resolver;


import github.slimjar.resolver.data.Repository;
import github.slimjar.resolver.pinger.URLPinger;
import github.slimjar.logging.LogDispatcher;
import github.slimjar.logging.ProcessLogger;
import github.slimjar.resolver.data.Dependency;
import github.slimjar.resolver.enquirer.RepositoryEnquirer;
import github.slimjar.resolver.enquirer.RepositoryEnquirerFactory;

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class CachingDependencyResolver implements DependencyResolver {
    private static final String FAILED_RESOLUTION_MESSAGE = "[FAILED TO RESOLVE]";
    private static final ProcessLogger LOGGER = LogDispatcher.getMediatingLogger();
    private final URLPinger urlPinger;
    private final Collection<RepositoryEnquirer> repositories;
    private final Map<Dependency, ResolutionResult> cachedResults = new ConcurrentHashMap<>();
    private final Map<String, ResolutionResult> preResolvedResults;

    public CachingDependencyResolver(final URLPinger urlPinger, final Collection<Repository> repositories, final RepositoryEnquirerFactory enquirerFactory, final Map<String, ResolutionResult> preResolvedResults) {
        this.urlPinger = urlPinger;
        this.preResolvedResults = new ConcurrentHashMap<>(preResolvedResults);
        this.repositories = repositories.stream()
                .map(enquirerFactory::create)
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<ResolutionResult> resolve(final Dependency dependency) {
        return Optional.ofNullable(cachedResults.computeIfAbsent(dependency, this::attemptResolve));
    }

    private ResolutionResult attemptResolve(final Dependency dependency) {
        final ResolutionResult preResolvedResult = preResolvedResults.get(dependency.toString());
        if (preResolvedResult != null) {
            if (preResolvedResult.isAggregator()) {
                return preResolvedResult;
            }
            final boolean isDependencyURLValid = urlPinger.ping(preResolvedResult.getDependencyURL());
            final URL checksumURL = preResolvedResult.getChecksumURL();
            final boolean isChecksumURLValid = checksumURL == null || urlPinger.ping(checksumURL);
            if (isDependencyURLValid && isChecksumURLValid) {
                return preResolvedResult;
            }
        }


        final Optional<ResolutionResult> result = repositories.stream().parallel()
                .map(enquirer -> enquirer.enquire(dependency))
                .filter(Objects::nonNull)
                .findFirst();
        final String resolvedResult = result.map(ResolutionResult::getDependencyURL).map(Objects::toString).orElse(FAILED_RESOLUTION_MESSAGE);
        LOGGER.debug("Resolved {0} @ {1}", dependency.getArtifactId(), resolvedResult);
        return result.orElse(null);
    }
}
