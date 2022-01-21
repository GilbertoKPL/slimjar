package github.slimjar.resolver.reader.dependency;


import github.slimjar.resolver.data.DependencyData;

import java.io.IOException;


@FunctionalInterface
public interface DependencyDataProvider {
    DependencyData get() throws IOException, ReflectiveOperationException;
}
