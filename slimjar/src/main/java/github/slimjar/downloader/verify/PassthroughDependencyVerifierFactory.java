package github.slimjar.downloader.verify;

import github.slimjar.resolver.DependencyResolver;
import github.slimjar.resolver.data.Dependency;

import java.io.File;
import java.io.IOException;

public final class PassthroughDependencyVerifierFactory implements DependencyVerifierFactory {
    @Override
    public DependencyVerifier create(DependencyResolver resolver) {
        return new PassthroughVerifier();
    }

    private static final class PassthroughVerifier implements DependencyVerifier {
        @Override
        public boolean verify(final File file, final Dependency dependency) throws IOException {
            return file.exists();
        }

        @Override
        public File getChecksumFile(Dependency dependency) {
            return null;
        }
    }
}
