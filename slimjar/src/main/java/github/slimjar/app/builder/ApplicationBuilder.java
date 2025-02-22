package github.slimjar.app.builder;

import github.slimjar.downloader.verify.*;
import github.slimjar.injector.DependencyInjector;
import github.slimjar.injector.DependencyInjectorFactory;
import github.slimjar.injector.SimpleDependencyInjectorFactory;
import github.slimjar.injector.helper.InjectionHelperFactory;
import github.slimjar.injector.loader.Injectable;
import github.slimjar.injector.loader.InjectableFactory;
import github.slimjar.injector.loader.IsolatedInjectableClassLoader;
import github.slimjar.logging.LogDispatcher;
import github.slimjar.logging.MediatingProcessLogger;
import github.slimjar.logging.ProcessLogger;
import github.slimjar.relocation.Relocator;
import github.slimjar.relocation.helper.RelocationHelper;
import github.slimjar.resolver.CachingDependencyResolverFactory;
import github.slimjar.resolver.DependencyResolver;
import github.slimjar.resolver.DependencyResolverFactory;
import github.slimjar.resolver.data.DependencyData;
import github.slimjar.resolver.data.Repository;
import github.slimjar.resolver.enquirer.RepositoryEnquirer;
import github.slimjar.resolver.pinger.HttpURLPinger;
import github.slimjar.resolver.pinger.URLPinger;
import github.slimjar.resolver.reader.dependency.DependencyDataProvider;
import github.slimjar.resolver.reader.dependency.DependencyDataProviderFactory;
import github.slimjar.resolver.reader.dependency.ExternalDependencyDataProviderFactory;
import github.slimjar.resolver.reader.dependency.GsonDependencyDataProviderFactory;
import github.slimjar.resolver.reader.facade.GsonFacadeFactory;
import github.slimjar.resolver.reader.facade.ReflectiveGsonFacadeFactory;
import github.slimjar.resolver.reader.resolution.PreResolutionDataProvider;
import github.slimjar.resolver.reader.resolution.PreResolutionDataProviderFactory;
import github.slimjar.app.Application;
import github.slimjar.downloader.DependencyDownloaderFactory;
import github.slimjar.downloader.URLDependencyDownloaderFactory;
import github.slimjar.downloader.output.DependencyOutputWriterFactory;
import github.slimjar.downloader.output.OutputWriterFactory;
import github.slimjar.downloader.strategy.ChecksumFilePathStrategy;
import github.slimjar.downloader.strategy.FilePathStrategy;
import github.slimjar.resolver.strategy.*;
import github.slimjar.relocation.JarFileRelocatorFactory;
import github.slimjar.relocation.RelocatorFactory;
import github.slimjar.relocation.facade.JarRelocatorFacadeFactory;
import github.slimjar.relocation.facade.ReflectiveJarRelocatorFacadeFactory;
import github.slimjar.relocation.helper.RelocationHelperFactory;
import github.slimjar.relocation.helper.VerifyingRelocationHelperFactory;
import github.slimjar.relocation.meta.FlatFileMetaMediatorFactory;
import github.slimjar.relocation.meta.MetaMediatorFactory;
import github.slimjar.resolver.enquirer.PingingRepositoryEnquirerFactory;
import github.slimjar.resolver.enquirer.RepositoryEnquirerFactory;
import github.slimjar.resolver.mirrors.MirrorSelector;
import github.slimjar.resolver.mirrors.SimpleMirrorSelector;
import github.slimjar.resolver.reader.resolution.GsonPreResolutionDataProviderFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * Serves as a configuration for different components slimjar will use during injection.
 * Allows completely modifying and adding upon onto the default behaviour when needed.
 */
public abstract class ApplicationBuilder {
    // Default directory where slimjar will try to install dependencies to
    // ~/.slimjar/
    private static final Path DEFAULT_DOWNLOAD_DIRECTORY;

    static {
        final String userHome = System.getProperty("user.home");
        final String defaultPath = String.format("%s/.slimjar", userHome);
        DEFAULT_DOWNLOAD_DIRECTORY = new File(defaultPath).toPath();
    }

    private final String applicationName;
    private URL dependencyFileUrl;
    private URL preResolutionFileUrl;
    private Path downloadDirectoryPath;
    private RelocatorFactory relocatorFactory;
    private DependencyDataProviderFactory moduleDataProviderFactory;
    private DependencyDataProviderFactory dataProviderFactory;
    private PreResolutionDataProviderFactory preResolutionDataProviderFactory;
    private RelocationHelperFactory relocationHelperFactory;
    private DependencyInjectorFactory injectorFactory;
    private DependencyResolverFactory resolverFactory;
    private RepositoryEnquirerFactory enquirerFactory;
    private DependencyDownloaderFactory downloaderFactory;
    private DependencyVerifierFactory verifierFactory;
    private MirrorSelector mirrorSelector;
    private Collection<Repository> internalRepositories;
    private ProcessLogger logger;

    /**
     * Generate a application builder for an application with given name.
     *
     * @param applicationName Name of your application/project. This exists to uniquely identify relocations.
     */
    protected ApplicationBuilder(final String applicationName) {
        this.applicationName = Objects.requireNonNull(applicationName, "Requires non-null application name!");
    }

    /**
     * Creates an ApplicationBuilder that allows jar-in-jar dependency loading.
     *
     * @param name   Name of your application/project. This exists to uniquely identify relocations.
     * @param config Basic configuration that isolated classloader requires.
     * @param args   Arguments to pass to created Application class (specified in <code>config</code>).
     * @return ApplicationBuilder that allows jar-in-jar dependency loading.
     */
    public static ApplicationBuilder isolated(final String name, final IsolationConfiguration config, Object[] args) {
        return new IsolatedApplicationBuilder(name, config, args);
    }

    /**
     * Creates an ApplicationBuilder that allows loading into current classloader.
     *
     * @param name Name of your application/project. This exists to uniquely identify relocations.
     * @return ApplicationBuilder that allows loading into current classloader.
     * @throws URISyntaxException           on invalid download path
     * @throws ReflectiveOperationException on attempting to use wrong appender, possible due to unexpected result in jvm version detection
     * @throws NoSuchAlgorithmException     on Selected/Default digest algorithm not existing.
     * @throws IOException                  on File IO failure
     */
    public static ApplicationBuilder appending(final String name) throws URISyntaxException, ReflectiveOperationException, NoSuchAlgorithmException, IOException {
        return InjectingApplicationBuilder.createAppending(name);
    }

    /**
     * Creates an ApplicationBuilder that allows loading into any given {@link Injectable} instance.
     * For a simple isolated classloader, use {@link IsolatedInjectableClassLoader}
     * You can create a {@link Injectable} version of any classloader using {@link InjectableFactory#create(Path, Collection)}
     * Alternatively you can provide a custom implementation of {@link Injectable} to specify how the dependencies must be added to the classloader.
     *
     * @param name Name of your application/project. This exists to uniquely identify relocations.
     * @return ApplicationBuilder that allows loading into any given {@link Injectable} instance.
     */
    public static ApplicationBuilder injecting(final String name, final Injectable injectable) {
        return new InjectingApplicationBuilder(name, injectable);
    }

    /**
     * URL to the json configuration file that defines the dependencies/repositories/mirrors
     *
     * @param dependencyFileUrl URL to the json configuration file (Default being plugin.json inside jar root generated by the gradle plugin)
     * @return <code>this</code>
     */
    public final ApplicationBuilder dependencyFileUrl(final URL dependencyFileUrl) {
        this.dependencyFileUrl = dependencyFileUrl;
        return this;
    }

    /**
     * URL to the json configuration file that defines the resolutions for given dependencies
     *
     * @param preResolutionFileUrl URL to the json resolution configuration file (Default being plugin-dependencies.json inside jar root generated by the gradle plugin)
     * @return <code>this</code>
     */
    public final ApplicationBuilder preResolutionFileUrl(final URL preResolutionFileUrl) {
        this.preResolutionFileUrl = preResolutionFileUrl;
        return this;
    }

    /**
     * Directory to which slimjar will attempt to download dependency files into
     *
     * @param downloadDirectoryPath Download directory for dependencies.
     * @return <code>this</code>
     */
    public final ApplicationBuilder downloadDirectoryPath(final Path downloadDirectoryPath) {
        this.downloadDirectoryPath = downloadDirectoryPath;
        return this;
    }

    /**
     * Factory class that defines the construction of {@link Relocator}
     * This deals with the actual relocation process.
     * The default implementation uses lucko/JarRelocator
     *
     * @param relocatorFactory Factory class to create Relocator
     * @return <code>this</code>
     */
    public final ApplicationBuilder relocatorFactory(final RelocatorFactory relocatorFactory) {
        this.relocatorFactory = relocatorFactory;
        return this;
    }

    /**
     * Factory that produces DataProvider for modules in jar-in-jar classloading. Ignored if not using jar-in-jar/isolated(...)
     * Used to fetch the `plugin.json` file of each submodule.
     *
     * @param moduleDataProviderFactory Factory that produces DataProvider for modules in jar-in-jar
     * @return <code>this</code>
     */
    public final ApplicationBuilder moduleDataProviderFactory(final DependencyDataProviderFactory moduleDataProviderFactory) {
        this.moduleDataProviderFactory = moduleDataProviderFactory;
        return this;
    }

    /**
     * Factory that produces {@link DependencyDataProvider} to handle `dependencyFileUrl` (by default plugin.json)
     * Used to fetch the `plugin.json` file of current jar-file.
     *
     * @param dataProviderFactory Factory that produces DataProvider to handle `dependencyFileUrl`
     * @return <code>this</code>
     */
    public final ApplicationBuilder dataProviderFactory(final DependencyDataProviderFactory dataProviderFactory) {
        this.dataProviderFactory = dataProviderFactory;
        return this;
    }

    /**
     * Factory that produces {@link PreResolutionDataProvider} to handle `preResolutionFileUrl` (by default plugin-dependencies.json)
     * Used to fetch the `plugin.json` file of current jar-file.
     *
     * @param preResolutionDataProviderFactory Factory that produces DataProvider to handle `preResolutionFileUrl`
     * @return <code>this</code>
     */
    public final ApplicationBuilder preResolutionDataProviderFactory(final PreResolutionDataProviderFactory preResolutionDataProviderFactory) {
        this.preResolutionDataProviderFactory = preResolutionDataProviderFactory;
        return this;
    }

    /**
     * Factory that produces a {@link RelocationHelper} using <code>relocator</code>
     * This is an abstraction over {@link Relocator}.
     * It decides the output file for relocation and includes extra steps such as jar verification.
     *
     * @param relocationHelperFactory Factory that produces a RelocationHelper
     * @return <code>this</code>
     */
    public final ApplicationBuilder relocationHelperFactory(final RelocationHelperFactory relocationHelperFactory) {
        this.relocationHelperFactory = relocationHelperFactory;
        return this;
    }

    /**
     * Factory that produces a {@link DependencyInjector} using <code>relocator</code>
     * {@link DependencyInjector} decides how any given {@link DependencyData} is injected into an {@link Injectable}
     *
     * @param injectorFactory Factory that produces a DependencyInjector
     * @return <code>this</code>
     */
    public final ApplicationBuilder injectorFactory(final DependencyInjectorFactory injectorFactory) {
        this.injectorFactory = injectorFactory;
        return this;
    }

    /**
     * Factory that produces a {@link DependencyResolverFactory}
     * {@link DependencyResolver} deals with resolving the URLs to a given dependency from a given collection of repositories
     *
     * @param resolverFactory Factory that produces a DependencyResolverFactory
     * @return <code>this</code>
     */
    public final ApplicationBuilder resolverFactory(final DependencyResolverFactory resolverFactory) {
        this.resolverFactory = resolverFactory;
        return this;
    }

    /**
     * Factory that produces a {@link RepositoryEnquirer}
     *
     * @param enquirerFactory Factory that produces a RepositoryEnquirer
     * @return <code>this</code>
     */
    public final ApplicationBuilder enquirerFactory(final RepositoryEnquirerFactory enquirerFactory) {
        this.enquirerFactory = enquirerFactory;
        return this;
    }

    /**
     * @param downloaderFactory
     * @return
     */
    public final ApplicationBuilder downloaderFactory(final DependencyDownloaderFactory downloaderFactory) {
        this.downloaderFactory = downloaderFactory;
        return this;
    }

    public final ApplicationBuilder verifierFactory(final DependencyVerifierFactory verifierFactory) {
        this.verifierFactory = verifierFactory;
        return this;
    }

    public final ApplicationBuilder mirrorSelector(final MirrorSelector mirrorSelector) {
        this.mirrorSelector = mirrorSelector;
        return this;
    }

    public final ApplicationBuilder internalRepositories(final Collection<Repository> repositories) {
        this.internalRepositories = repositories;
        return this;
    }

    public final ApplicationBuilder logger(final ProcessLogger logger) {
        this.logger = logger;
        return this;
    }

    protected final String getApplicationName() {
        return applicationName;
    }

    protected final URL getDependencyFileUrl() {
        if (dependencyFileUrl == null) {
            this.dependencyFileUrl = getClass().getClassLoader().getResource("plugin.json");
        }
        return dependencyFileUrl;
    }

    protected final URL getPreResolutionFileUrl() {
        if (preResolutionFileUrl == null) {
            this.preResolutionFileUrl = getClass().getClassLoader().getResource("plugin-dependencies.json");
        }
        return preResolutionFileUrl;
    }

    protected final Path getDownloadDirectoryPath() {
        if (downloadDirectoryPath == null) {
            this.downloadDirectoryPath = DEFAULT_DOWNLOAD_DIRECTORY;
        }
        return downloadDirectoryPath;
    }

    protected final RelocatorFactory getRelocatorFactory() throws ReflectiveOperationException, NoSuchAlgorithmException, IOException, URISyntaxException {
        if (relocatorFactory == null) {
            final JarRelocatorFacadeFactory jarRelocatorFacadeFactory = ReflectiveJarRelocatorFacadeFactory.create(getDownloadDirectoryPath(), getInternalRepositories());
            this.relocatorFactory = new JarFileRelocatorFactory(jarRelocatorFacadeFactory);
        }
        return relocatorFactory;
    }

    protected final DependencyDataProviderFactory getModuleDataProviderFactory() throws URISyntaxException, ReflectiveOperationException, NoSuchAlgorithmException, IOException {
        if (moduleDataProviderFactory == null) {
            final GsonFacadeFactory gsonFacadeFactory = ReflectiveGsonFacadeFactory.create(getDownloadDirectoryPath(), getInternalRepositories());
            this.moduleDataProviderFactory = new ExternalDependencyDataProviderFactory(gsonFacadeFactory);
        }
        return moduleDataProviderFactory;
    }

    protected final DependencyDataProviderFactory getDataProviderFactory() throws URISyntaxException, ReflectiveOperationException, NoSuchAlgorithmException, IOException {
        if (dataProviderFactory == null) {
            final GsonFacadeFactory gsonFacadeFactory = ReflectiveGsonFacadeFactory.create(getDownloadDirectoryPath(), getInternalRepositories());
            this.dataProviderFactory = new GsonDependencyDataProviderFactory(gsonFacadeFactory);
        }
        return dataProviderFactory;
    }

    protected final PreResolutionDataProviderFactory getPreResolutionDataProviderFactory() throws URISyntaxException, ReflectiveOperationException, NoSuchAlgorithmException, IOException {
        if (preResolutionDataProviderFactory == null) {
            final GsonFacadeFactory gsonFacadeFactory = ReflectiveGsonFacadeFactory.create(getDownloadDirectoryPath(), getInternalRepositories());
            this.preResolutionDataProviderFactory = new GsonPreResolutionDataProviderFactory(gsonFacadeFactory);
        }
        return preResolutionDataProviderFactory;
    }

    protected final RelocationHelperFactory getRelocationHelperFactory() throws NoSuchAlgorithmException, IOException, URISyntaxException {
        if (relocationHelperFactory == null) {
            final FileChecksumCalculator checksumCalculator = new FileChecksumCalculator("SHA-256");
            final FilePathStrategy pathStrategy = FilePathStrategy.createRelocationStrategy(getDownloadDirectoryPath().toFile(), getApplicationName());
            final MetaMediatorFactory mediatorFactory = new FlatFileMetaMediatorFactory();
            this.relocationHelperFactory = new VerifyingRelocationHelperFactory(checksumCalculator, pathStrategy, mediatorFactory);
        }
        return relocationHelperFactory;
    }

    protected final DependencyInjectorFactory getInjectorFactory() {
        if (injectorFactory == null) {
            this.injectorFactory = new SimpleDependencyInjectorFactory();
        }
        return injectorFactory;
    }

    protected final DependencyResolverFactory getResolverFactory() {
        if (resolverFactory == null) {
            final URLPinger pinger = new HttpURLPinger();
            this.resolverFactory = new CachingDependencyResolverFactory(pinger);
        }
        return resolverFactory;
    }

    protected final RepositoryEnquirerFactory getEnquirerFactory() {
        if (enquirerFactory == null) {
            final PathResolutionStrategy releaseStrategy = new MavenPathResolutionStrategy();
            final PathResolutionStrategy snapshotStrategy = new MavenSnapshotPathResolutionStrategy();
            final PathResolutionStrategy resolutionStrategy = new MediatingPathResolutionStrategy(releaseStrategy, snapshotStrategy);
            final PathResolutionStrategy pomURLCreationStrategy = new MavenPomPathResolutionStrategy();
            final PathResolutionStrategy checksumResolutionStrategy = new MavenChecksumPathResolutionStrategy("SHA-1", resolutionStrategy);
            final URLPinger urlPinger = new HttpURLPinger();
            this.enquirerFactory = new PingingRepositoryEnquirerFactory(resolutionStrategy, checksumResolutionStrategy, pomURLCreationStrategy, urlPinger);
        }
        return enquirerFactory;
    }

    protected final DependencyDownloaderFactory getDownloaderFactory() {
        if (downloaderFactory == null) {
            this.downloaderFactory = new URLDependencyDownloaderFactory();
        }
        return downloaderFactory;
    }

    protected final DependencyVerifierFactory getVerifierFactory() throws NoSuchAlgorithmException {
        if (verifierFactory == null) {
            final FilePathStrategy filePathStrategy = ChecksumFilePathStrategy.createStrategy(getDownloadDirectoryPath().toFile(), "SHA-1");
            final OutputWriterFactory checksumOutputFactory = new DependencyOutputWriterFactory(filePathStrategy);
            final DependencyVerifierFactory fallback = new PassthroughDependencyVerifierFactory();
            final ChecksumCalculator checksumCalculator = new FileChecksumCalculator("SHA-1");
            this.verifierFactory = new ChecksumDependencyVerifierFactory(checksumOutputFactory, fallback, checksumCalculator);
        }
        return verifierFactory;
    }

    protected final MirrorSelector getMirrorSelector() throws MalformedURLException {
        if (mirrorSelector == null) {
            mirrorSelector = new SimpleMirrorSelector(getInternalRepositories());
        }
        return mirrorSelector;
    }

    protected final Collection<Repository> getInternalRepositories() throws MalformedURLException {
        if (internalRepositories == null) {
            internalRepositories = Collections.singleton(new Repository(new URL(SimpleMirrorSelector.ALT_CENTRAL_URL)));
        }
        return internalRepositories;
    }

    protected final ProcessLogger getLogger() {
        if (logger == null) {
            logger = (msg, args) -> {
            };
        }
        return logger;
    }

    protected final DependencyInjector createInjector() throws IOException, URISyntaxException, NoSuchAlgorithmException, ReflectiveOperationException {
        final InjectionHelperFactory injectionHelperFactory = new InjectionHelperFactory(
                getDownloadDirectoryPath(),
                getRelocatorFactory(),
                getDataProviderFactory(),
                getRelocationHelperFactory(),
                getInjectorFactory(),
                getResolverFactory(),
                getEnquirerFactory(),
                getDownloaderFactory(),
                getVerifierFactory(),
                getMirrorSelector()
        );
        return getInjectorFactory().create(injectionHelperFactory);
    }

    public final Application build() throws IOException, ReflectiveOperationException, URISyntaxException, NoSuchAlgorithmException {
        final MediatingProcessLogger mediatingLogger = LogDispatcher.getMediatingLogger();
        final ProcessLogger logger = getLogger();
        mediatingLogger.addLogger(logger);
        final Application result = buildApplication();
        mediatingLogger.removeLogger(logger);
        return result;
    }

    protected abstract Application buildApplication() throws IOException, ReflectiveOperationException, URISyntaxException, NoSuchAlgorithmException;

}
