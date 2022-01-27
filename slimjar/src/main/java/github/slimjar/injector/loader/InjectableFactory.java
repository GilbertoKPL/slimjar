package github.slimjar.injector.loader;

import github.slimjar.resolver.data.Repository;
import github.slimjar.app.builder.ApplicationBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

public final class InjectableFactory {
    private InjectableFactory() {
    }

    public static Injectable create(final Path downloadPath, final Collection<Repository> repositories) throws ReflectiveOperationException, NoSuchAlgorithmException, IOException, URISyntaxException, ExecutionException, InterruptedException {
        return create(downloadPath, repositories, InjectableFactory.class.getClassLoader());
    }

    public static Injectable create(final Path downloadPath, final Collection<Repository> repositories, final ClassLoader classLoader) throws URISyntaxException, ReflectiveOperationException, NoSuchAlgorithmException, IOException, ExecutionException, InterruptedException {
        final boolean isJigsawActive = isJigsawActive();
        Injectable injectable = null;

        if (isJigsawActive && classLoader instanceof URLClassLoader) {
            injectable = new WrappedInjectableClassLoader((URLClassLoader) ApplicationBuilder.class.getClassLoader());
        } else if (isUnsafeAvailable() && classLoader instanceof URLClassLoader) {
            try {
                injectable = UnsafeInjectable.create((URLClassLoader) classLoader);
            } catch (final Exception exception) {
                // failed to prepare injectable with unsafe, ignored exception to let it silently switch to fallback agent injection
            }
        }

        if (injectable == null) {
            injectable = InstrumentationInjectable.create(downloadPath, repositories);
        }
        return injectable;
    }

    private static boolean isJigsawActive() {
        try {
            Class.forName("java.lang.Module");
        } catch (final ClassNotFoundException e) {
            return true;
        }
        return false;
    }

    private static boolean isUnsafeAvailable() {
        try {
            Class.forName("sun.misc.Unsafe");
        } catch (final ClassNotFoundException e) {
            return false;
        }
        return true;
    }
}
