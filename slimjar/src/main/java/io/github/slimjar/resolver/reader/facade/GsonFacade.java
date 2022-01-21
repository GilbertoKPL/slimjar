package io.github.slimjar.resolver.reader.facade;

import java.io.InputStreamReader;
import java.lang.reflect.Type;

public interface GsonFacade {
    <T> T fromJson(final InputStreamReader reader, final Class<T> clazz) throws ReflectiveOperationException;

    <T> T fromJson(final InputStreamReader reader, final Type rawType) throws ReflectiveOperationException;
}
