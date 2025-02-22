package github.slimjar.injector;

import github.slimjar.injector.helper.InjectionHelperFactory;

public final class SimpleDependencyInjectorFactory implements DependencyInjectorFactory {
    @Override
    public DependencyInjector create(final InjectionHelperFactory injectionHelperFactory) {
        return new SimpleDependencyInjector(injectionHelperFactory);
    }
}
