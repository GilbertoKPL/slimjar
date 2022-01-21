package io.github.slimjar.resolver.mirrors;

import io.github.slimjar.resolver.data.Mirror;
import io.github.slimjar.resolver.data.Repository;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public final class SimpleMirrorSelector implements MirrorSelector {
    public static final String DEFAULT_CENTRAL_MIRROR_URL = "https://repo.vshnv.tech/";
    public static final String CENTRAL_URL = "https://repo.maven.apache.org/maven2/";
    public static final String ALT_CENTRAL_URL = "https://repo1.maven.org/maven2/";
    private static final Collection<String> CENTRAL_REPO = Arrays.asList(CENTRAL_URL, ALT_CENTRAL_URL);
    private final Collection<Repository> centralMirrors;

    public SimpleMirrorSelector(final Collection<Repository> centralMirrors) {
        this.centralMirrors = centralMirrors;
    }

    private static boolean isCentral(final Repository repository) {
        final String url = repository.getUrl().toString();
        return CENTRAL_REPO.contains(url);
    }

    @Override
    public Collection<Repository> select(final Collection<Repository> mainRepositories, final Collection<Mirror> mirrors) throws MalformedURLException {
        final Collection<URL> originals = mirrors.stream()
                .map(Mirror::getOriginal)
                .collect(Collectors.toSet());
        final Collection<Repository> resolved = mainRepositories.stream()
                .filter(repo -> !originals.contains(repo.getUrl()))
                .filter(repo -> !isCentral(repo))
                .collect(Collectors.toSet());
        final Collection<Repository> mirrored = mirrors.stream()
                .map(Mirror::getMirroring)
                .map(Repository::new)
                .collect(Collectors.toSet());
        resolved.addAll(mirrored);
        resolved.addAll(centralMirrors);
        return resolved;
    }
}
