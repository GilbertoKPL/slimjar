package github.slimjar.resolver.reader.dependency;

import java.net.URL;

@FunctionalInterface
public interface DependencyDataProviderFactory {
    DependencyDataProvider create(final URL dependencyFileURL);
}
