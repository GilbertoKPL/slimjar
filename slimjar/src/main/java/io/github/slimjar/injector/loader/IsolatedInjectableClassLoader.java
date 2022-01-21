package io.github.slimjar.injector.loader;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class IsolatedInjectableClassLoader extends InjectableClassLoader {
    private final Map<String, Class<?>> delegatesMap = new HashMap<>();

    public IsolatedInjectableClassLoader() {
        this(new URL[0]);
    }

    public IsolatedInjectableClassLoader(final URL... urls) {
        this(urls, Collections.emptySet());
    }

    public IsolatedInjectableClassLoader(final URL[] urls, final Collection<Class<?>> delegates) {
        this(urls, getSystemClassLoader().getParent(), delegates);
    }

    public IsolatedInjectableClassLoader(final URL[] urls, final ClassLoader parent, final Collection<Class<?>> delegates) {
        super(urls, parent);
        for (Class<?> clazz : delegates) {
            delegatesMap.put(clazz.getName(), clazz);
        }
    }


    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // Check if class was already loaded
        final Class<?> loaded = findLoadedClass(name);
        if (loaded != null) {
            return loaded;
        }
        // Check if its any of provided classes
        final Class<?> delegate = delegatesMap.get(name);
        if (delegate != null) {
            return delegate;
        }

        // Load from 'URLClassPath's
        return super.loadClass(name, resolve);
    }

}
