package github.slimjar.resolver.reader.facade;

public interface GsonFacadeFactory {
    GsonFacade createFacade() throws ReflectiveOperationException;
}
