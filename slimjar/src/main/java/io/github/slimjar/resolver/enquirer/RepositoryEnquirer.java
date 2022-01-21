package io.github.slimjar.resolver.enquirer;

import io.github.slimjar.resolver.ResolutionResult;
import io.github.slimjar.resolver.data.Dependency;

public interface RepositoryEnquirer {
    ResolutionResult enquire(final Dependency dependency);
}
