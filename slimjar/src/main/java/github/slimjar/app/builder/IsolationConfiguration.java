package github.slimjar.app.builder;

import github.slimjar.app.module.ModuleExtractor;
import github.slimjar.app.module.TemporaryModuleExtractor;
import github.slimjar.util.Modules;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public final class IsolationConfiguration {
    private final String applicationClass;
    private final Collection<String> modules;
    private final ClassLoader parentClassloader;
    private final ModuleExtractor moduleExtractor;

    public IsolationConfiguration(String applicationClass, Collection<String> modules, ClassLoader parentClassloader, ModuleExtractor moduleExtractor) {
        this.applicationClass = applicationClass;
        this.modules = Collections.unmodifiableCollection(modules);
        this.parentClassloader = parentClassloader;
        this.moduleExtractor = moduleExtractor;
    }

    public static Builder builder(final String applicationClass) {
        return new Builder().applicationClass(applicationClass);
    }

    public String getApplicationClass() {
        return applicationClass;
    }

    public Collection<String> getModules() {
        return modules;
    }

    public ClassLoader getParentClassloader() {
        return parentClassloader;
    }

    public ModuleExtractor getModuleExtractor() {
        return moduleExtractor;
    }

    public static final class Builder {
        private String applicationClass;
        private Collection<String> modules = new HashSet<>();
        private ClassLoader parentClassloader;
        private ModuleExtractor moduleExtractor;

        public Builder applicationClass(final String applicationClass) {
            this.applicationClass = applicationClass;
            return this;
        }

        public Builder modules(final Collection<String> modules) {
            final Collection<String> mod = new HashSet<>(modules);
            mod.addAll(modules);
            this.modules = mod;
            return this;
        }

        public Builder module(final String module) {
            this.modules.add(module);
            return this;
        }

        public Builder parentClassLoader(final ClassLoader classLoader) {
            this.parentClassloader = classLoader;
            return this;
        }

        public Builder moduleExtractor(final ModuleExtractor moduleExtractor) {
            this.moduleExtractor = moduleExtractor;
            return this;
        }

        String getApplicationClass() {
            if (applicationClass == null) {
                throw new AssertionError("Application Class not Provided!");
            }
            return applicationClass;
        }

        Collection<String> getModules() throws IOException, URISyntaxException {
            if (modules == null || modules.isEmpty()) {
                this.modules = Modules.findLocalModules();
            }
            return modules;
        }

        ClassLoader getParentClassloader() {
            if (parentClassloader == null) {
                this.parentClassloader = ClassLoader.getSystemClassLoader().getParent();
            }
            return parentClassloader;
        }

        ModuleExtractor getModuleExtractor() {
            if (moduleExtractor == null) {
                this.moduleExtractor = new TemporaryModuleExtractor();
            }
            return moduleExtractor;
        }

        public IsolationConfiguration build() throws IOException, URISyntaxException {
            return new IsolationConfiguration(getApplicationClass(), getModules(), getParentClassloader(), getModuleExtractor());
        }
    }
}
