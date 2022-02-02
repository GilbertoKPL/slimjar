package github.slimjar.app.builder;

import github.slimjar.app.Application;
import github.slimjar.injector.DependencyInjector;
import github.slimjar.injector.loader.InjectableClassLoader;
import github.slimjar.injector.loader.IsolatedInjectableClassLoader;
import github.slimjar.resolver.ResolutionResult;
import github.slimjar.resolver.data.DependencyData;
import github.slimjar.resolver.reader.dependency.DependencyDataProvider;
import github.slimjar.resolver.reader.resolution.PreResolutionDataProvider;
import github.slimjar.util.Modules;
import github.slimjar.util.Parameters;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Map;

public final class IsolatedApplicationBuilder extends ApplicationBuilder {
    private final IsolationConfiguration isolationConfiguration;
    private final Object[] arguments;

    public IsolatedApplicationBuilder(final String applicationName, final IsolationConfiguration isolationConfiguration, final Object[] arguments) {
        super(applicationName);
        this.isolationConfiguration = isolationConfiguration;
        this.arguments = arguments.clone();
    }

    @Override
    public Application buildApplication() throws IOException, ReflectiveOperationException, URISyntaxException, NoSuchAlgorithmException {
        final DependencyInjector injector = createInjector();
        final URL[] moduleUrls = Modules.extract(isolationConfiguration.getModuleExtractor(), isolationConfiguration.getModules());

        final InjectableClassLoader classLoader = new IsolatedInjectableClassLoader(moduleUrls, isolationConfiguration.getParentClassloader(), Collections.singleton(Application.class));

        final DependencyDataProvider dataProvider = getDataProviderFactory().create(getDependencyFileUrl());
        final DependencyData selfDependencyData = dataProvider.get();

        final PreResolutionDataProvider preResolutionDataProvider = getPreResolutionDataProviderFactory().create(getPreResolutionFileUrl());
        final Map<String, ResolutionResult> preResolutionResultMap = preResolutionDataProvider.get();

        injector.inject(classLoader, selfDependencyData, preResolutionResultMap);

        for (final URL module : moduleUrls) {
            final DependencyDataProvider moduleDataProvider = getModuleDataProviderFactory().create(module);
            final DependencyData dependencyData = moduleDataProvider.get();
            // TODO:: fetch isolated pre-resolutions
            injector.inject(classLoader, dependencyData, preResolutionResultMap);
        }

        final Class<Application> applicationClass = (Class<Application>) Class.forName(isolationConfiguration.getApplicationClass(), true, classLoader);

        // TODO:: Fix constructor resolution
        return applicationClass.getConstructor(Parameters.typesFrom(arguments)).newInstance(arguments);
    }

}
