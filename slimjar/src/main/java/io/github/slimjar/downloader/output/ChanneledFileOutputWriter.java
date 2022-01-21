package io.github.slimjar.downloader.output;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ChanneledFileOutputWriter implements OutputWriter {
    private static final Logger LOGGER = Logger.getLogger(ChanneledFileOutputWriter.class.getName());
    private final File outputFile;

    public ChanneledFileOutputWriter(final File outputFile) {
        this.outputFile = outputFile;
    }

    @Override
    public File writeFrom(final InputStream inputStream, final long length) throws IOException {
        LOGGER.log(Level.FINE, "Attempting to write from inputStream...");
        if (!outputFile.exists()) {
            LOGGER.log(Level.FINE, "Writing {0} bytes...", length);
            try (final ReadableByteChannel channel = Channels.newChannel(inputStream)) {
                try (final FileOutputStream output = new FileOutputStream(outputFile)) {
                    output.getChannel().transferFrom(channel, 0, length);
                }
            }
        }
        inputStream.close();
        return outputFile;
    }
}
