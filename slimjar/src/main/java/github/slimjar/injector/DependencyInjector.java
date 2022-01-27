package github.slimjar.injector;

import github.slimjar.injector.loader.Injectable;
import github.slimjar.resolver.ResolutionResult;
import github.slimjar.resolver.data.DependencyData;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public interface DependencyInjector {
    void inject(final Injectable injectable, final DependencyData data, final Map<String, ResolutionResult> preResolvedResults) throws InjectionFailedException, ReflectiveOperationException, NoSuchAlgorithmException, IOException, URISyntaxException, ExecutionException, InterruptedException;
}
