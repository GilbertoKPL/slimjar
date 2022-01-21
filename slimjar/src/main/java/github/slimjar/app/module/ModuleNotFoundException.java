package github.slimjar.app.module;


public final class ModuleNotFoundException extends RuntimeException {
    public ModuleNotFoundException(String moduleName) {
        super("Could not find module in jar: " + moduleName);
    }
}
