package github.slimjar.relocation.facade;

import github.slimjar.app.builder.ApplicationBuilder;
import github.slimjar.injector.loader.InjectableClassLoader;
import github.slimjar.injector.loader.IsolatedInjectableClassLoader;
import github.slimjar.resolver.data.Dependency;
import github.slimjar.resolver.data.DependencyData;
import github.slimjar.resolver.data.Repository;
import github.slimjar.util.Packages;
import github.slimjar.relocation.PassthroughRelocator;
import github.slimjar.relocation.RelocationRule;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

public final class ReflectiveJarRelocatorFacadeFactory implements JarRelocatorFacadeFactory {
    private static final String JAR_RELOCATOR_PACKAGE = "me#lucko#jarrelocator#JarRelocator";
    private static final String RELOCATION_PACKAGE = "me#lucko#jarrelocator#Relocation";

    private final Constructor<?> jarRelocatorConstructor;
    private final Constructor<?> relocationConstructor;
    private final Method jarRelocatorRunMethod;

    private ReflectiveJarRelocatorFacadeFactory(final Constructor<?> jarRelocatorConstructor, final Constructor<?> relocationConstructor, final Method jarRelocatorRunMethod) {
        this.jarRelocatorConstructor = jarRelocatorConstructor;
        this.relocationConstructor = relocationConstructor;
        this.jarRelocatorRunMethod = jarRelocatorRunMethod;
    }

    private static Object createRelocation(final Constructor<?> relocationConstructor, final RelocationRule rule) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        return relocationConstructor.newInstance(rule.getOriginalPackagePattern(), rule.getRelocatedPackagePattern(), rule.getExclusions(), rule.getInclusions());
    }

    private static Object createRelocator(final Constructor<?> jarRelocatorConstructor, final File input, final File output, final Collection<Object> rules) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        return jarRelocatorConstructor.newInstance(input, output, rules);
    }

    private static DependencyData getJarRelocatorDependency(final Collection<Repository> repositories) throws MalformedURLException {
        final Dependency asm = new Dependency(
                Packages.fix("org#ow2#asm"),
                "asm",
                "9.2",
                null,
                new HashSet<>()
        );
        final Dependency asmCommons = new Dependency(
                Packages.fix("org#ow2#asm"),
                "asm-commons",
                "9.2",
                null,
                new HashSet<>()
        );
        final Dependency jarRelocator = new Dependency(
                Packages.fix("me#lucko"),
                "jar-relocator",
                "1.5",
                null,
                new HashSet<>(Arrays.asList(asm, asmCommons))
        );
        return new DependencyData(
                Collections.emptySet(),
                repositories,
                Collections.singleton(jarRelocator),
                Collections.emptyList()
        );
    }

    public static JarRelocatorFacadeFactory create(final Path downloadPath, final Collection<Repository> repositories) throws URISyntaxException, ReflectiveOperationException, NoSuchAlgorithmException, IOException, ExecutionException, InterruptedException {
        final InjectableClassLoader classLoader = new IsolatedInjectableClassLoader();
        return create(downloadPath, repositories, classLoader);
    }

    public static JarRelocatorFacadeFactory create(final Path downloadPath, final Collection<Repository> repositories, final InjectableClassLoader classLoader) throws URISyntaxException, ReflectiveOperationException, NoSuchAlgorithmException, IOException, ExecutionException, InterruptedException {
        ApplicationBuilder.injecting("SlimJar", classLoader)
                .downloadDirectoryPath(downloadPath)
                .preResolutionDataProviderFactory(a -> Collections::emptyMap)
                .dataProviderFactory((url) -> () -> ReflectiveJarRelocatorFacadeFactory.getJarRelocatorDependency(repositories))
                .relocatorFactory((rules) -> new PassthroughRelocator())
                .relocationHelperFactory((relocator) -> (dependency, file) -> file)
                .build();
        final Class<?> jarRelocatorClass = Class.forName(Packages.fix(JAR_RELOCATOR_PACKAGE), true, classLoader);
        final Class<?> relocationClass = Class.forName(Packages.fix(RELOCATION_PACKAGE), true, classLoader);
        final Constructor<?> jarRelocatorConstructor = jarRelocatorClass.getConstructor(File.class, File.class, Collection.class);
        final Constructor<?> relocationConstructor = relocationClass.getConstructor(String.class, String.class, Collection.class, Collection.class);
        final Method runMethod = jarRelocatorClass.getMethod("run");
        return new ReflectiveJarRelocatorFacadeFactory(jarRelocatorConstructor, relocationConstructor, runMethod);
    }

    @Override
    public JarRelocatorFacade createFacade(final File input, final File output, final Collection<RelocationRule> relocationRules) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        final Collection<Object> relocations = new HashSet<>();
        for (final RelocationRule rule : relocationRules) {
            relocations.add(createRelocation(relocationConstructor, rule));
        }
        final Object relocator = createRelocator(jarRelocatorConstructor, input, output, relocations);
        return new ReflectiveJarRelocatorFacade(relocator, jarRelocatorRunMethod);
    }
}
