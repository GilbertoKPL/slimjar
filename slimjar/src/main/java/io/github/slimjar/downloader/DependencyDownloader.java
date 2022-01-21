package io.github.slimjar.downloader;

import io.github.slimjar.resolver.data.Dependency;

import java.io.File;
import java.io.IOException;

public interface DependencyDownloader {
    File download(final Dependency dependency) throws IOException;
}
