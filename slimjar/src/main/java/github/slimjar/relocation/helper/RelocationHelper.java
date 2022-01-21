package github.slimjar.relocation.helper;

import github.slimjar.resolver.data.Dependency;

import java.io.File;
import java.io.IOException;

public interface RelocationHelper {
    File relocate(final Dependency dependency, final File file) throws IOException, ReflectiveOperationException;
}
