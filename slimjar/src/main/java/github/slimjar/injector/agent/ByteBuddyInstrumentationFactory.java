package github.slimjar.injector.agent;

import github.slimjar.injector.loader.InjectableClassLoader;
import github.slimjar.injector.loader.InstrumentationInjectable;
import github.slimjar.injector.loader.IsolatedInjectableClassLoader;
import github.slimjar.injector.loader.manifest.JarManifestGenerator;
import github.slimjar.resolver.data.Dependency;
import github.slimjar.resolver.data.DependencyData;
import github.slimjar.resolver.data.Repository;
import github.slimjar.app.builder.ApplicationBuilder;
import github.slimjar.app.module.ModuleExtractor;
import github.slimjar.app.module.TemporaryModuleExtractor;
import github.slimjar.relocation.JarFileRelocator;
import github.slimjar.relocation.PassthroughRelocator;
import github.slimjar.relocation.RelocationRule;
import github.slimjar.relocation.Relocator;
import github.slimjar.relocation.facade.JarRelocatorFacadeFactory;
import github.slimjar.resolver.mirrors.SimpleMirrorSelector;
import github.slimjar.util.Packages;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public final class ByteBuddyInstrumentationFactory implements InstrumentationFactory {
    public static final String AGENT_JAR = "loader-agent.isolated-jar";
    private static final String AGENT_PACKAGE = "io#github#slimjar#injector#agent";
    private static final String AGENT_CLASS = "ClassLoaderAgent";
    private static final String BYTE_BUDDY_AGENT_CLASS = "net#bytebuddy#agent#ByteBuddyAgent";

    private final URL agentJarUrl;
    private final ModuleExtractor extractor;
    private final JarRelocatorFacadeFactory relocatorFacadeFactory;

    public ByteBuddyInstrumentationFactory(final JarRelocatorFacadeFactory relocatorFacadeFactory) throws ReflectiveOperationException, NoSuchAlgorithmException, IOException, URISyntaxException {
        this.relocatorFacadeFactory = relocatorFacadeFactory;
        this.agentJarUrl = InstrumentationInjectable.class.getClassLoader().getResource(AGENT_JAR);
        this.extractor = new TemporaryModuleExtractor();
    }


    public ByteBuddyInstrumentationFactory(final URL agentJarUrl, final ModuleExtractor extractor, final JarRelocatorFacadeFactory relocatorFacadeFactory) {
        this.agentJarUrl = agentJarUrl;
        this.extractor = extractor;
        this.relocatorFacadeFactory = relocatorFacadeFactory;
    }

    private static DependencyData getDependency() throws MalformedURLException {
        final Dependency byteBuddy = new Dependency(
                "net.bytebuddy",
                "byte-buddy-agent",
                "1.12.7",
                null,
                new HashSet<>()
        );
        final Repository centralRepository = new Repository(new URL(SimpleMirrorSelector.CENTRAL_URL));
        return new DependencyData(
                Collections.emptySet(),
                Collections.singleton(centralRepository),
                Collections.singleton(byteBuddy),
                Collections.emptyList()
        );
    }

    private static String generatePattern() {
        return String.format("slimjar.%s", UUID.randomUUID());
    }

    @Override
    public Instrumentation create() throws IOException, ReflectiveOperationException, URISyntaxException, NoSuchAlgorithmException, ExecutionException, InterruptedException {
        final URL extractedURL = extractor.extractModule(agentJarUrl, "loader-agent");

        final String pattern = generatePattern();
        final String relocatedAgentClass = String.format("%s.%s", pattern, AGENT_CLASS);
        final RelocationRule relocationRule = new RelocationRule(Packages.fix(AGENT_PACKAGE), pattern, Collections.emptySet(), Collections.emptySet());
        final Relocator relocator = new JarFileRelocator(Collections.singleton(relocationRule), relocatorFacadeFactory);
        final File inputFile = new File(extractedURL.toURI());
        final File relocatedFile = File.createTempFile("slimjar-agent", ".jar");

        final InjectableClassLoader classLoader = new IsolatedInjectableClassLoader();
        relocator.relocate(inputFile, relocatedFile);

        JarManifestGenerator.with(relocatedFile.toURI())
                .attribute("Manifest-Version", "1.0")
                .attribute("Agent-Class", relocatedAgentClass)
                .generate();

        ApplicationBuilder.injecting("SlimJar-Agent", classLoader)
                .dataProviderFactory((dataUrl) -> ByteBuddyInstrumentationFactory::getDependency)
                .relocatorFactory((rules) -> new PassthroughRelocator())
                .relocationHelperFactory((rel) -> (dependency, file) -> file)
                .build();

        final Class<?> byteBuddyAgentClass = Class.forName(Packages.fix(BYTE_BUDDY_AGENT_CLASS), true, classLoader);
        final Method attachMethod = byteBuddyAgentClass.getMethod("attach", File.class, String.class, String.class);

        final Class<?> processHandle = Class.forName("java.lang.ProcessHandle");
        final Method currentMethod = processHandle.getMethod("current");
        final Method pidMethod = processHandle.getMethod("pid");
        final Object currentProcess = currentMethod.invoke(processHandle);
        final Long processId = (Long) pidMethod.invoke(currentProcess);


        attachMethod.invoke(null, relocatedFile, String.valueOf(processId), "");

        final Class<?> agentClass = Class.forName(relocatedAgentClass, true, ClassLoader.getSystemClassLoader());
        final Method instrMethod = agentClass.getMethod("getInstrumentation");
        return (Instrumentation) instrMethod.invoke(null);
    }
}
