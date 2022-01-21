package io.github.slimjar.injector.loader.manifest;

import java.io.IOException;

public interface ManifestGenerator {
    ManifestGenerator attribute(final String key, final String value);

    void generate() throws IOException;
}
