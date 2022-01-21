package github.slimjar.resolver.data;

import java.util.Objects;
import java.util.Set;

public final class Dependency {
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String snapshotId;
    private final Set<Dependency> transitive;

    public Dependency(String groupId, String artifactId, String version, String snapshotId, Set<Dependency> transitive) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.snapshotId = snapshotId;
        this.transitive = transitive;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getSnapshotId() {
        return snapshotId;
    }

    public Set<Dependency> getTransitive() {
        return transitive;
    }

    @Override
    public String toString() {
        final String snapshotId = getSnapshotId();
        final String suffix = (snapshotId != null && snapshotId.length() > 0) ? (":" + snapshotId) : "";
        return getGroupId() + ":" + getArtifactId() + ":" + getVersion() + suffix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dependency that = (Dependency) o;
        return groupId.equals(that.groupId) &&
                artifactId.equals(that.artifactId) &&
                version.equals(that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, version);
    }

}
