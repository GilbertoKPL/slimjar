package github.slimjar.injector.loader;

import github.slimjar.resolver.data.Repository;
import github.slimjar.injector.agent.ByteBuddyInstrumentationFactory;
import github.slimjar.injector.agent.InstrumentationFactory;
import github.slimjar.relocation.facade.ReflectiveJarRelocatorFacadeFactory;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.jar.JarFile;

public final class InstrumentationInjectable implements Injectable {


    private final Instrumentation instrumentation;

    public InstrumentationInjectable(final Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    public static Injectable create(final Path downloadPath, final Collection<Repository> repositories) throws IOException, NoSuchAlgorithmException, ReflectiveOperationException, URISyntaxException, ExecutionException, InterruptedException {
        return create(new ByteBuddyInstrumentationFactory(ReflectiveJarRelocatorFacadeFactory.create(downloadPath, repositories)));
    }

    public static Injectable create(final InstrumentationFactory factory) throws IOException, NoSuchAlgorithmException, ReflectiveOperationException, URISyntaxException, ExecutionException, InterruptedException {
        return new InstrumentationInjectable(factory.create());
    }

    @Override
    public void inject(final URL url) throws IOException, URISyntaxException {
        instrumentation.appendToSystemClassLoaderSearch(new JarFile(new File(url.toURI())));
    }
}
