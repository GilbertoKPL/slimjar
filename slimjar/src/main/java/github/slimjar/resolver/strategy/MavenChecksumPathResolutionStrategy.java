package github.slimjar.resolver.strategy;

import github.slimjar.resolver.data.Dependency;
import github.slimjar.resolver.data.Repository;

import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

public final class MavenChecksumPathResolutionStrategy implements PathResolutionStrategy {
    private final PathResolutionStrategy resolutionStrategy;
    private final String algorithm;

    public MavenChecksumPathResolutionStrategy(final String algorithm, final PathResolutionStrategy resolutionStrategy) {
        this.algorithm = algorithm.replaceAll("[ -]", "").toLowerCase(Locale.ENGLISH);
        this.resolutionStrategy = resolutionStrategy;
    }

    @Override
    public Collection<String> pathTo(final Repository repository, final Dependency dependency) {
        return resolutionStrategy.pathTo(repository, dependency).stream().map(path -> path + "." + algorithm).collect(Collectors.toSet());
    }
}
