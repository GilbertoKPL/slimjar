package github.slimjar.injector.helper;


import github.slimjar.resolver.data.Dependency;
import github.slimjar.downloader.DependencyDownloader;
import github.slimjar.relocation.helper.RelocationHelper;

import java.io.File;
import java.io.IOException;

public final class InjectionHelper {
    private final DependencyDownloader dependencyDownloader;
    private final RelocationHelper relocationHelper;

    public InjectionHelper(final DependencyDownloader dependencyDownloader, final RelocationHelper relocationHelper) {
        this.dependencyDownloader = dependencyDownloader;
        this.relocationHelper = relocationHelper;
    }

    public File fetch(final Dependency dependency) throws IOException, ReflectiveOperationException {
        final File downloaded = dependencyDownloader.download(dependency);
        if (downloaded == null) {
            return null;
        }
        return relocationHelper.relocate(dependency, downloaded);
    }
}
