package github.slimjar.app.builder;

import github.slimjar.injector.DependencyInjector;
import github.slimjar.injector.loader.Injectable;
import github.slimjar.injector.loader.InjectableFactory;
import github.slimjar.resolver.ResolutionResult;
import github.slimjar.resolver.data.DependencyData;
import github.slimjar.resolver.reader.dependency.DependencyDataProvider;
import github.slimjar.resolver.reader.resolution.PreResolutionDataProvider;
import github.slimjar.app.AppendingApplication;
import github.slimjar.app.Application;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public final class InjectingApplicationBuilder extends ApplicationBuilder {
    private final Function<ApplicationBuilder, Injectable> injectableSupplier;

    public InjectingApplicationBuilder(final String applicationName, final Injectable injectable) {
        this(applicationName, (it) -> injectable);
    }

    public InjectingApplicationBuilder(final String applicationName, final Function<ApplicationBuilder, Injectable> injectableSupplier) {
        super(applicationName);
        this.injectableSupplier = injectableSupplier;
    }

    public static ApplicationBuilder createAppending(final String applicationName) throws ReflectiveOperationException, NoSuchAlgorithmException, IOException, URISyntaxException {
        final ClassLoader classLoader = ApplicationBuilder.class.getClassLoader();
        return createAppending(applicationName, classLoader);
    }

    public static ApplicationBuilder createAppending(final String applicationName, final ClassLoader classLoader) throws ReflectiveOperationException, NoSuchAlgorithmException, IOException, URISyntaxException {
        return new InjectingApplicationBuilder(applicationName, (ApplicationBuilder builder) -> {
            try {
                return InjectableFactory.create(builder.getDownloadDirectoryPath(), builder.getInternalRepositories(), classLoader);
            } catch (URISyntaxException | ReflectiveOperationException | NoSuchAlgorithmException | IOException | ExecutionException | InterruptedException exception) {
                exception.printStackTrace();
            }
            return null;
        });
    }

    @Override
    public Application buildApplication() throws IOException, ReflectiveOperationException, URISyntaxException, NoSuchAlgorithmException, ExecutionException, InterruptedException {
        final DependencyDataProvider dataProvider = getDataProviderFactory().create(getDependencyFileUrl());
        final DependencyData dependencyData = dataProvider.get();
        final DependencyInjector dependencyInjector = createInjector();

        final PreResolutionDataProvider preResolutionDataProvider = getPreResolutionDataProviderFactory().create(getPreResolutionFileUrl());
        final Map<String, ResolutionResult> preResolutionResultMap = preResolutionDataProvider.get();

        dependencyInjector.inject(injectableSupplier.apply(this), dependencyData, preResolutionResultMap);
        return new AppendingApplication();
    }
}

