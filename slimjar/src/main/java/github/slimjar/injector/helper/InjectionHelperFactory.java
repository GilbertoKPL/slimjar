package github.slimjar.injector.helper;

import github.slimjar.resolver.DependencyResolver;
import github.slimjar.resolver.DependencyResolverFactory;
import github.slimjar.resolver.ResolutionResult;
import github.slimjar.resolver.data.DependencyData;
import github.slimjar.resolver.data.Repository;
import github.slimjar.resolver.reader.dependency.DependencyDataProviderFactory;
import github.slimjar.downloader.DependencyDownloader;
import github.slimjar.downloader.DependencyDownloaderFactory;
import github.slimjar.downloader.output.DependencyOutputWriterFactory;
import github.slimjar.downloader.output.OutputWriterFactory;
import github.slimjar.downloader.strategy.FilePathStrategy;
import github.slimjar.downloader.verify.DependencyVerifierFactory;
import github.slimjar.injector.DependencyInjectorFactory;
import github.slimjar.relocation.Relocator;
import github.slimjar.relocation.RelocatorFactory;
import github.slimjar.relocation.helper.RelocationHelper;
import github.slimjar.relocation.helper.RelocationHelperFactory;
import github.slimjar.resolver.enquirer.RepositoryEnquirerFactory;
import github.slimjar.resolver.mirrors.MirrorSelector;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Map;

public final class InjectionHelperFactory {
    private final Path downloadDirectoryPath;
    private final RelocatorFactory relocatorFactory;
    private final RelocationHelperFactory relocationHelperFactory;
    private final DependencyResolverFactory resolverFactory;
    private final RepositoryEnquirerFactory enquirerFactory;
    private final DependencyDownloaderFactory downloaderFactory;
    private final DependencyVerifierFactory verifier;
    private final MirrorSelector mirrorSelector;

    public InjectionHelperFactory(Path downloadDirectoryPath, RelocatorFactory relocatorFactory, DependencyDataProviderFactory dataProviderFactory, RelocationHelperFactory relocationHelperFactory, DependencyInjectorFactory injectorFactory, DependencyResolverFactory resolverFactory, RepositoryEnquirerFactory enquirerFactory, DependencyDownloaderFactory downloaderFactory, DependencyVerifierFactory verifier, MirrorSelector mirrorSelector) {
        this.downloadDirectoryPath = downloadDirectoryPath;
        this.relocatorFactory = relocatorFactory;
        this.relocationHelperFactory = relocationHelperFactory;
        this.resolverFactory = resolverFactory;
        this.enquirerFactory = enquirerFactory;
        this.downloaderFactory = downloaderFactory;
        this.verifier = verifier;
        this.mirrorSelector = mirrorSelector;
    }

    public InjectionHelper create(final DependencyData data, final Map<String, ResolutionResult> preResolvedResults) throws IOException, NoSuchAlgorithmException, URISyntaxException {
        final Collection<Repository> repositories = mirrorSelector
                .select(data.getRepositories(), data.getMirrors());
        final Relocator relocator = relocatorFactory.create(data.getRelocations());
        final RelocationHelper relocationHelper = relocationHelperFactory.create(relocator);
        final FilePathStrategy filePathStrategy = FilePathStrategy.createDefault(downloadDirectoryPath.toFile());
        final OutputWriterFactory outputWriterFactory = new DependencyOutputWriterFactory(filePathStrategy);
        final DependencyResolver resolver = resolverFactory.create(repositories, preResolvedResults, enquirerFactory);
        final DependencyDownloader downloader = downloaderFactory.create(outputWriterFactory, resolver, verifier.create(resolver));
        return new InjectionHelper(downloader, relocationHelper);
    }
}
