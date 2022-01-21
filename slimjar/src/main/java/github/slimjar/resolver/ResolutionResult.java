package github.slimjar.resolver;

import github.slimjar.resolver.data.Repository;

import java.net.URL;
import java.util.Objects;

public final class ResolutionResult {
    private final Repository repository;
    private final URL dependencyURL;
    private final URL checksumURL;
    private final boolean isAggregator;

    public ResolutionResult(final Repository repository, final URL dependencyURL, final URL checksumURL, final boolean isAggregator) {
        this.repository = repository;
        this.dependencyURL = dependencyURL;
        this.checksumURL = checksumURL;
        this.isAggregator = isAggregator;
        if (!isAggregator) {
            Objects.requireNonNull(dependencyURL, "Resolved URL must not be null for non-aggregator dependencies");
        }
    }

    public Repository getRepository() {
        return repository;
    }

    public URL getDependencyURL() {
        return dependencyURL;
    }

    public URL getChecksumURL() {
        return checksumURL;
    }

    public boolean isAggregator() {
        return isAggregator;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResolutionResult that = (ResolutionResult) o;
        // String comparison to avoid all blocking calls
        return dependencyURL.toString().equals(that.toString()) &&
                Objects.equals(checksumURL.toString(), that.getChecksumURL().toString()) &&
                isAggregator == that.isAggregator;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependencyURL.toString(), checksumURL.toString(), isAggregator);
    }
}
