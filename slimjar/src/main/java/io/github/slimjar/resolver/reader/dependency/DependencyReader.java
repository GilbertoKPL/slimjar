package io.github.slimjar.resolver.reader.dependency;


import io.github.slimjar.resolver.data.DependencyData;

import java.io.IOException;
import java.io.InputStream;

public interface DependencyReader {
    DependencyData read(InputStream inputStream) throws IOException, ReflectiveOperationException;
}
