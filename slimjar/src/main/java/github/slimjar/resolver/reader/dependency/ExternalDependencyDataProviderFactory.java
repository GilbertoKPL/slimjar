package github.slimjar.resolver.reader.dependency;

import github.slimjar.resolver.reader.facade.GsonFacadeFactory;
import github.slimjar.resolver.reader.facade.GsonFacade;

import java.net.URL;

public final class ExternalDependencyDataProviderFactory implements DependencyDataProviderFactory {
    private final GsonFacade gson;

    public ExternalDependencyDataProviderFactory(final GsonFacadeFactory gsonFactory) throws ReflectiveOperationException {
        this.gson = gsonFactory.createFacade();
    }

    public ExternalDependencyDataProviderFactory(final GsonFacade gson) {
        this.gson = gson;
    }


    public DependencyDataProvider create(final URL dependencyFileURL) {
        final DependencyReader dependencyReader = new GsonDependencyReader(gson);
        return new ModuleDependencyDataProvider(dependencyReader, dependencyFileURL);
    }
}