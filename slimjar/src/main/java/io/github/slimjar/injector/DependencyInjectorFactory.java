package io.github.slimjar.injector;

import io.github.slimjar.injector.helper.InjectionHelperFactory;

@FunctionalInterface
public interface DependencyInjectorFactory {
    DependencyInjector create(final InjectionHelperFactory injectionHelperFactory);
}
