package io.github.slimjar.injector;

import io.github.slimjar.injector.helper.InjectionHelper;
import io.github.slimjar.injector.helper.InjectionHelperFactory;
import io.github.slimjar.injector.loader.Injectable;
import io.github.slimjar.resolver.ResolutionResult;
import io.github.slimjar.resolver.data.Dependency;
import io.github.slimjar.resolver.data.DependencyData;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Map;

public final class SimpleDependencyInjector implements DependencyInjector {
    private final InjectionHelperFactory injectionHelperFactory;

    public SimpleDependencyInjector(final InjectionHelperFactory injectionHelperFactory) {
        this.injectionHelperFactory = injectionHelperFactory;
    }

    @Override
    public void inject(final Injectable injectable, final DependencyData data, final Map<String, ResolutionResult> preResolvedResults) throws ReflectiveOperationException, NoSuchAlgorithmException, IOException, URISyntaxException {
        final InjectionHelper helper = injectionHelperFactory.create(data, preResolvedResults);
        injectDependencies(injectable, helper, data.getDependencies());
    }

    private void injectDependencies(final Injectable injectable, final InjectionHelper injectionHelper, final Collection<Dependency> dependencies) throws ReflectiveOperationException {
        for (final Dependency dependency : dependencies) {
            try {
                final File depJar = injectionHelper.fetch(dependency);
                if (depJar == null) {
                    continue;
                }
                injectable.inject(depJar.toURI().toURL());
                injectDependencies(injectable, injectionHelper, dependency.getTransitive());
            } catch (final IOException e) {
                throw new InjectionFailedException(dependency, e);
            } catch (IllegalAccessException | InvocationTargetException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }
}
