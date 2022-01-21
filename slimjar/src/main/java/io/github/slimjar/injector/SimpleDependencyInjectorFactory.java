package io.github.slimjar.injector;

import io.github.slimjar.injector.helper.InjectionHelperFactory;

public final class SimpleDependencyInjectorFactory implements DependencyInjectorFactory {
    @Override
    public DependencyInjector create(final InjectionHelperFactory injectionHelperFactory) {
        return new SimpleDependencyInjector(injectionHelperFactory);
    }
}
