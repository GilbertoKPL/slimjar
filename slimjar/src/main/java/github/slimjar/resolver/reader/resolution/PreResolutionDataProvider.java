package github.slimjar.resolver.reader.resolution;

import github.slimjar.resolver.ResolutionResult;

import java.io.IOException;
import java.util.Map;

@FunctionalInterface
public interface PreResolutionDataProvider {
    Map<String, ResolutionResult> get() throws IOException, ReflectiveOperationException;
}
