package io.github.slimjar.resolver.reader.dependency;


import io.github.slimjar.resolver.data.DependencyData;

import java.io.IOException;


@FunctionalInterface
public interface DependencyDataProvider {
    DependencyData get() throws IOException, ReflectiveOperationException;
}
