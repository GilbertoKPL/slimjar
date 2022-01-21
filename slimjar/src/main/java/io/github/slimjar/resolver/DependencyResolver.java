package io.github.slimjar.resolver;


import io.github.slimjar.resolver.data.Dependency;

import java.util.Optional;

@FunctionalInterface
public interface DependencyResolver {
    Optional<ResolutionResult> resolve(final Dependency dependency);
}
