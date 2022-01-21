package io.github.slimjar.injector;

import io.github.slimjar.injector.loader.Injectable;
import io.github.slimjar.resolver.ResolutionResult;
import io.github.slimjar.resolver.data.DependencyData;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;


public interface DependencyInjector {
    void inject(final Injectable injectable, final DependencyData data, final Map<String, ResolutionResult> preResolvedResults) throws InjectionFailedException, ReflectiveOperationException, NoSuchAlgorithmException, IOException, URISyntaxException;
}
