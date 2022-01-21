package github.slimjar.resolver.enquirer;

import github.slimjar.resolver.ResolutionResult;
import github.slimjar.resolver.data.Dependency;

public interface RepositoryEnquirer {
    ResolutionResult enquire(final Dependency dependency);
}
