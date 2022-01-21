package github.slimjar.downloader.verify;

import github.slimjar.resolver.data.Dependency;

import java.io.File;
import java.io.IOException;

public interface DependencyVerifier {
    boolean verify(final File file, final Dependency dependency) throws IOException;

    File getChecksumFile(final Dependency dependency);
}
