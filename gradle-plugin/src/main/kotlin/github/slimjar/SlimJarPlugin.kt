package github.slimjar

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import github.slimjar.exceptions.ShadowNotFoundException
import github.slimjar.func.applyReleaseRepo
import github.slimjar.func.applySnapshotRepo
import github.slimjar.func.createConfig
import github.slimjar.func.slim
import github.slimjar.task.SlimJar
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import org.gradle.api.plugins.JavaPlugin
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.maven

const val SLIM_CONFIGURATION_NAME = "slim"
const val SLIM_API_CONFIGURATION_NAME = "slimApi"
const val SLIM_JAR_TASK_NAME = "slimJar"
private const val RESOURCES_TASK = "processResources"
private const val SHADOW_ID = "com.github.johnrengelman.shadow"

class SlimJarPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        // Applies Java if not present, since it's required for the compileOnly configuration
        plugins.apply(JavaPlugin::class.java)

        if (!plugins.hasPlugin(SHADOW_ID)) {
            throw ShadowNotFoundException("SlimJar depends on the Shadow plugin, please apply the plugin. For more information visit: https://imperceptiblethoughts.com/shadow/")
        }

        val slimConfig = createConfig(
            SLIM_CONFIGURATION_NAME,
            JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME,
            JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME
        )
        if (plugins.hasPlugin("java-library")) {
            createConfig(
                SLIM_API_CONFIGURATION_NAME,
                JavaPlugin.COMPILE_ONLY_API_CONFIGURATION_NAME,
                JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME
            )
        }

        val slimJar = tasks.create(SLIM_JAR_TASK_NAME, SlimJar::class.java, slimConfig)
        // Auto adds the slimJar lib dependency
        afterEvaluate {
            if (applyReleaseRepo) {
                repositories.maven("https://repo.vshnv.tech/")
            }
            if (applySnapshotRepo) {
                repositories.maven("https://repo.vshnv.tech/snapshots/")
            }
            if (plugins.hasPlugin("java-library")) {
                scanSlim(project).forEach {
                    project.dependencies.slim(it)
                }
            }
        }
        project.dependencies.extra.set(
            "slimjar",
            asGroovyClosure("+") { version -> "github.slimjar:slimjar:$version" }
        )
        // Hooks into shadow to inject relocations
        val shadowTask = tasks.withType(ShadowJar::class.java).firstOrNull() ?: return
        shadowTask.doFirst {
            slimJar.relocations().forEach { rule ->
                shadowTask.relocate(rule.originalPackagePattern, rule.relocatedPackagePattern) {
                    rule.inclusions.forEach { include(it) }
                    rule.exclusions.forEach { exclude(it) }
                }
            }
        }

        /*slimJar.outputs.upToDateWhen {
            true
        }*/

        // Runs the task once resources are being processed to save the json file
        tasks.findByName(RESOURCES_TASK)?.finalizedBy(slimJar)
    }

    private fun scanSlim(project: Project): Collection<Dependency> {
        val found = HashSet<Dependency>()
        val impl = project.configurations.findByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME)
        if (impl == null) {
            return emptyList()
        }
        impl.dependencies
            .filterIsInstance<DefaultProjectDependency>()
            .map { it.dependencyProject }
            .forEach {
                found.addAll(scanSlim(it))
                val slim = it.configurations.findByName(SLIM_CONFIGURATION_NAME)
                if (slim == null) {
                    return@forEach
                }
                slim.dependencies
                    .filterNotNull()
                    .forEach { dep ->
                        found.add(dep)
                    }
            }
        return found
    }
}
