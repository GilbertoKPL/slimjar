@file:Suppress("UNCHECKED_CAST")

package github.slimjar.func

import github.slimjar.exceptions.ConfigurationNotFoundException
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.DependencyHandlerScope

/**
 * Checks in the gradle.properties if should or not resolve dependencies at compile time
 */
val Project.performCompileTimeResolution: Boolean
    get() = findProperty("slimjar.default.resolution.compile_time")?.toString()?.toBoolean() ?: true

/**
 * Checks in the gradle.properties if should or not add the slimJar repo by default
 */
val Project.applyReleaseRepo: Boolean
    get() = findProperty("slimjar.default.repo.releases.apply")?.toString()?.toBoolean() ?: true


/**
 * Checks in the gradle.properties if should or not add the slimJar snapshot repo by default
 */
val Project.applySnapshotRepo: Boolean
    get() = findProperty("slimjar.default.repo.snapshot.apply")?.toString()?.toBoolean() ?: false

/**
 * Checks in the gradle.properties if should or not add the slimJar plugin to isolated projects by default
 */
val Project.slimInjectToIsolated: Boolean
    get() = findProperty("slimjar.default.isolated.inject")?.toString()?.toBoolean() ?: true


/**
 * Utility for creating a configuration that extends another
 */
fun Project.createConfig(configName: String, vararg extends: String): Configuration {
    val compileOnlyConfig = extends.map {
        configurations.findByName(it)
            ?: throw ConfigurationNotFoundException("Could not find `$extends` configuration!")
    }

    val slimConfig = configurations.create(configName)
    compileOnlyConfig.forEach { it.extendsFrom(slimConfig) }
    slimConfig.isTransitive = true

    return slimConfig
}

/**
 * Extension for KDSL support
 */
fun DependencyHandlerScope.slimjar(version: String = "+"): String =
    (extensions.getByName("slimjar") as? (String) -> String)?.let { it(version) }
        ?: throw IllegalStateException()

/**
 * Extension for KDSL support
 */
fun DependencyHandler.slimjar(version: String = "+"): String =
    (extensions.getByName("slimjar") as? (String) -> String)?.let { it(version) }
        ?: throw IllegalStateException()
