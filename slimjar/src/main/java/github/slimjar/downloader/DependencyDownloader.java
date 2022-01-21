package github.slimjar.downloader;

import github.slimjar.resolver.data.Dependency;

import java.io.File;
import java.io.IOException;

public interface DependencyDownloader {
    File download(final Dependency dependency) throws IOException;
}
