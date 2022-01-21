package github.slimjar.resolver.reader.resolution;

import github.slimjar.resolver.ResolutionResult;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface PreResolutionDataReader {
    Map<String, ResolutionResult> read(InputStream inputStream) throws IOException, ReflectiveOperationException;
}
