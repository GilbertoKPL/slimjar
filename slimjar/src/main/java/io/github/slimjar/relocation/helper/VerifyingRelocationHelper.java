package io.github.slimjar.relocation.helper;

import io.github.slimjar.downloader.strategy.FilePathStrategy;
import io.github.slimjar.relocation.Relocator;
import io.github.slimjar.relocation.meta.MetaMediator;
import io.github.slimjar.relocation.meta.MetaMediatorFactory;
import io.github.slimjar.resolver.data.Dependency;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

public final class VerifyingRelocationHelper implements RelocationHelper {
    private final FilePathStrategy outputFilePathStrategy;
    private final Relocator relocator;
    private final String selfHash;
    private final MetaMediatorFactory mediatorFactory;

    public VerifyingRelocationHelper(final String selfHash, final FilePathStrategy outputFilePathStrategy, final Relocator relocator, final MetaMediatorFactory mediatorFactory) throws URISyntaxException, NoSuchAlgorithmException, IOException {
        this.mediatorFactory = mediatorFactory;
        this.outputFilePathStrategy = outputFilePathStrategy;
        this.relocator = relocator;
        this.selfHash = selfHash;
    }

    @Override
    public File relocate(final Dependency dependency, final File file) throws IOException, ReflectiveOperationException {
        final File relocatedFile = outputFilePathStrategy.selectFileFor(dependency);
        final MetaMediator metaMediator = mediatorFactory.create(relocatedFile.toPath());
        if (relocatedFile.exists()) {
            try {
                final String ownerHash = metaMediator.readAttribute("slimjar.owner");
                if (selfHash != null && ownerHash != null && selfHash.trim().equals(ownerHash.trim())) {
                    return relocatedFile;
                }
            } catch (final Exception exception) {
                // Possible incomplete relocation present.
                // todo: Log incident
                //noinspection ResultOfMethodCallIgnored
                relocatedFile.delete();
            }
        }
        relocator.relocate(file, relocatedFile);
        metaMediator.writeAttribute("slimjar.owner", selfHash);
        return relocatedFile;
    }
}
