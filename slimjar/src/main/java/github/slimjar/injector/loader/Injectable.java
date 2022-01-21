package github.slimjar.injector.loader;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;

public interface Injectable {
    static WrappedInjectableClassLoader wrap(final URLClassLoader classLoader) {
        return new WrappedInjectableClassLoader(classLoader);
    }

    void inject(final URL url) throws IOException, InvocationTargetException, IllegalAccessException, URISyntaxException;
}
