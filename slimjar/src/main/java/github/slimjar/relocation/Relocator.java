package github.slimjar.relocation;

import java.io.File;
import java.io.IOException;

public interface Relocator {
    void relocate(final File input, final File output) throws IOException, ReflectiveOperationException;
}
