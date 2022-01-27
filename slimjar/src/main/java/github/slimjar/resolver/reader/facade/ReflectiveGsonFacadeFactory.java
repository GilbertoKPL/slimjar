package github.slimjar.resolver.reader.facade;

import github.slimjar.app.builder.ApplicationBuilder;
import github.slimjar.injector.loader.InjectableClassLoader;
import github.slimjar.injector.loader.IsolatedInjectableClassLoader;
import github.slimjar.relocation.PassthroughRelocator;
import github.slimjar.resolver.data.Dependency;
import github.slimjar.resolver.data.DependencyData;
import github.slimjar.resolver.data.Repository;
import github.slimjar.util.Packages;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

public final class ReflectiveGsonFacadeFactory implements GsonFacadeFactory {
    private static final String GSON_PACKAGE = "com#google#gson#Gson";
    private static final String GSON_TYPES_PACKAGE = "com#google#gson#internal#$Gson$Types";

    private final Constructor<?> gsonConstructor;
    private final Method gsonFromJsonMethod;
    private final Method gsonFromJsonTypeMethod;
    private final Method canonicalizeMethod;

    private ReflectiveGsonFacadeFactory(final Constructor<?> gsonConstructor, final Method gsonFromJsonMethod, final Method gsonFromJsonTypeMethod, final Method canonicalizeMethod) {
        this.gsonConstructor = gsonConstructor;
        this.gsonFromJsonMethod = gsonFromJsonMethod;
        this.gsonFromJsonTypeMethod = gsonFromJsonTypeMethod;
        this.canonicalizeMethod = canonicalizeMethod;
    }

    public static GsonFacadeFactory create(final Path downloadPath, final Collection<Repository> repositories) throws ReflectiveOperationException, NoSuchAlgorithmException, IOException, URISyntaxException, ExecutionException, InterruptedException {
        final InjectableClassLoader classLoader = new IsolatedInjectableClassLoader();
        return create(downloadPath, repositories, classLoader);
    }

    public static GsonFacadeFactory create(final Path downloadPath, final Collection<Repository> repositories, final InjectableClassLoader classLoader) throws ReflectiveOperationException, NoSuchAlgorithmException, IOException, URISyntaxException, ExecutionException, InterruptedException {
        ApplicationBuilder.injecting("SlimJar", classLoader)
                .downloadDirectoryPath(downloadPath)
                .dataProviderFactory((url) -> () -> ReflectiveGsonFacadeFactory.getGsonDependency(repositories))
                .relocatorFactory((rules) -> new PassthroughRelocator())
                .preResolutionDataProviderFactory(a -> Collections::emptyMap)
                .relocationHelperFactory((relocator) -> (dependency, file) -> file)
                .build();
        final Class<?> gsonClass = Class.forName(Packages.fix(GSON_PACKAGE), true, classLoader);
        final Constructor<?> gsonConstructor = gsonClass.getConstructor();
        final Method gsonFromJsonMethod = gsonClass.getMethod("fromJson", Reader.class, Class.class);
        final Method gsonFromJsonTypeMethod = gsonClass.getMethod("fromJson", Reader.class, Type.class);

        final Class<?> gsonTypesClass = Class.forName(Packages.fix(GSON_TYPES_PACKAGE), true, classLoader);

        final Method canonicalizeMethod = gsonTypesClass.getMethod("canonicalize", Type.class);
        return new ReflectiveGsonFacadeFactory(gsonConstructor, gsonFromJsonMethod, gsonFromJsonTypeMethod, canonicalizeMethod);
    }

    private static DependencyData getGsonDependency(final Collection<Repository> repositories) throws MalformedURLException {
        final Dependency gson = new Dependency(
                Packages.fix("com#google#code#gson"),
                "gson",
                "2.8.9",
                null,
                new HashSet<>()
        );
        return new DependencyData(
                Collections.emptySet(),
                repositories,
                Collections.singleton(gson),
                Collections.emptyList()
        );
    }

    @Override
    public GsonFacade createFacade() throws ReflectiveOperationException {
        final Object gson = gsonConstructor.newInstance();
        return new ReflectiveGsonFacade(gson, gsonFromJsonMethod, gsonFromJsonTypeMethod, canonicalizeMethod);
    }
}
