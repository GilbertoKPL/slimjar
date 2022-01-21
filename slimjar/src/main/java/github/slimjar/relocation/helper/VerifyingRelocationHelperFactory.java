package github.slimjar.relocation.helper;

import github.slimjar.downloader.strategy.FilePathStrategy;
import github.slimjar.downloader.verify.FileChecksumCalculator;
import github.slimjar.relocation.Relocator;
import github.slimjar.relocation.meta.MetaMediatorFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

public class VerifyingRelocationHelperFactory implements RelocationHelperFactory {
    private static final URL JAR_URL = VerifyingRelocationHelperFactory.class.getProtectionDomain().getCodeSource().getLocation();

    private final FilePathStrategy relocationFilePathStrategy;
    private final MetaMediatorFactory mediatorFactory;
    private final String selfHash;

    public VerifyingRelocationHelperFactory(final String selfHash, final FilePathStrategy relocationFilePathStrategy, final MetaMediatorFactory mediatorFactory) {
        this.relocationFilePathStrategy = relocationFilePathStrategy;
        this.mediatorFactory = mediatorFactory;
        this.selfHash = selfHash;
    }

    public VerifyingRelocationHelperFactory(final FileChecksumCalculator calculator, final FilePathStrategy relocationFilePathStrategy, final MetaMediatorFactory mediatorFactory) throws URISyntaxException, IOException {
        this(calculator.calculate(new File(JAR_URL.toURI())), relocationFilePathStrategy, mediatorFactory);
    }

    @Override
    public RelocationHelper create(final Relocator relocator) throws URISyntaxException, IOException, NoSuchAlgorithmException {
        return new VerifyingRelocationHelper(selfHash, relocationFilePathStrategy, relocator, mediatorFactory);
    }
}
