package github.slimjar.injector;

import github.slimjar.injector.helper.InjectionHelperFactory;

@FunctionalInterface
public interface DependencyInjectorFactory {
    DependencyInjector create(final InjectionHelperFactory injectionHelperFactory);
}
