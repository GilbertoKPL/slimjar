package io.github.slimjar.downloader.output;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface OutputWriter {
    File writeFrom(final InputStream inputStream, final long length) throws IOException;
}
