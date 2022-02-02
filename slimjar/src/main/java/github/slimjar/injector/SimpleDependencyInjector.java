package github.slimjar.injector;

import github.slimjar.injector.helper.InjectionHelper;
import github.slimjar.injector.helper.InjectionHelperFactory;
import github.slimjar.injector.loader.Injectable;
import github.slimjar.resolver.ResolutionResult;
import github.slimjar.resolver.data.Dependency;
import github.slimjar.resolver.data.DependencyData;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
        ArrayList<CompletableFuture<Void>> list = new ArrayList<>();
        int value = 0;
        for (final Dependency dependency : dependencies) {
            value += 1;
            if (value < 3) {
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
            list.add(CompletableFuture.runAsync(
                    () -> {
                        try {
                            final File depJar = injectionHelper.fetch(dependency);
                            if (depJar == null) {
                                return;
                            }
                            injectable.inject(depJar.toURI().toURL());
                            injectDependencies(injectable, injectionHelper, dependency.getTransitive());
                        } catch (final IOException | ReflectiveOperationException e) {
                            throw new InjectionFailedException(dependency, e);
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
            ));
        }
        list.forEach(CompletableFuture::join);
    }
}
