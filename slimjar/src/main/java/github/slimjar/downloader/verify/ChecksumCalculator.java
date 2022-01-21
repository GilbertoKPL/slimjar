package github.slimjar.downloader.verify;

import java.io.File;
import java.io.IOException;

public interface ChecksumCalculator {
    String calculate(final File file) throws IOException;
}
