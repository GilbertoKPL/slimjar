package github.slimjar.downloader.verify;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class FileChecksumCalculator implements ChecksumCalculator {
    private static final String DIRECTORY_HASH = "DIRECTORY";
    private static final Logger LOGGER = Logger.getLogger(FileChecksumCalculator.class.getName());
    private final MessageDigest digest;

    public FileChecksumCalculator(final String algorithm) throws NoSuchAlgorithmException {
        digest = MessageDigest.getInstance(algorithm);
    }

    @Override
    public String calculate(final File file) throws IOException {
        LOGGER.log(Level.FINEST, "Calculating hash for {0}", file.getPath());
        // This helps run IDE environment as a special case
        if (file.isDirectory()) {
            return DIRECTORY_HASH;
        }
        digest.reset();
        try (final FileInputStream fis = new FileInputStream(file)) {
            byte[] byteArray = new byte[1024];
            int bytesCount;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        }
        byte[] bytes = digest.digest();

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        sb.trimToSize();
        final String result = sb.toString();
        LOGGER.log(Level.FINEST, "Hash for {0} -> {1}", new Object[]{file.getPath(), result});
        return result;
    }
}
