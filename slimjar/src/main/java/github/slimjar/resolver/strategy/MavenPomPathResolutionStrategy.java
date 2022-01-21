package github.slimjar.resolver.strategy;

import github.slimjar.resolver.data.Dependency;
import github.slimjar.resolver.data.Repository;
import github.slimjar.util.Repositories;

import java.util.Collection;
import java.util.Collections;

public final class MavenPomPathResolutionStrategy implements PathResolutionStrategy {
    private static final String PATH_FORMAT = "%s%s/%s/%s/%3$s-%4$s.pom";

    @Override
    public Collection<String> pathTo(Repository repository, Dependency dependency) {
        final String repoUrl = Repositories.fetchFormattedUrl(repository);
        return Collections.singleton(
                String.format(
                        PATH_FORMAT,
                        repoUrl,
                        dependency.getGroupId().replace('.', '/'),
                        dependency.getArtifactId(),
                        dependency.getVersion()
                )
        );
    }
}
