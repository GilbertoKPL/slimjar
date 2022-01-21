package io.github.slimjar.resolver.reader.facade;

import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public final class ReflectiveGsonFacade implements GsonFacade {
    private final Object gson;
    private final Method gsonFromJsonMethod;
    private final Method gsonFromJsonTypeMethod;
    private final Method canonicalizeMethod;

    ReflectiveGsonFacade(final Object gson, final Method gsonFromJsonMethod, final Method gsonFromJsonTypeMethod, final Method canonicalizeMethod) {
        this.gson = gson;
        this.gsonFromJsonMethod = gsonFromJsonMethod;
        this.gsonFromJsonTypeMethod = gsonFromJsonTypeMethod;
        this.canonicalizeMethod = canonicalizeMethod;
    }

    @Override
    public <T> T fromJson(InputStreamReader reader, Class<T> clazz) throws ReflectiveOperationException {
        final Object result = gsonFromJsonMethod.invoke(gson, reader, clazz);
        if (clazz.isAssignableFrom(result.getClass())) {
            return (T) result;
        } else {
            throw new AssertionError("Gson returned wrong type!");
        }
    }

    @Override
    public <T> T fromJson(InputStreamReader reader, Type rawType) throws ReflectiveOperationException {
        final Object canonicalizedType = canonicalizeMethod.invoke(null, rawType);
        final Object result = gsonFromJsonTypeMethod.invoke(gson, reader, canonicalizedType);
        return (T) result;
    }
}
