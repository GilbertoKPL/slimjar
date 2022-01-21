package github.slimjar.resolver.strategy;

import github.slimjar.resolver.data.Dependency;
import github.slimjar.resolver.data.Repository;

import java.util.Collection;

public interface PathResolutionStrategy {
    Collection<String> pathTo(final Repository repository, final Dependency dependency);
}
