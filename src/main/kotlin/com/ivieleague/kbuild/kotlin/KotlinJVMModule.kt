package com.ivieleague.kbuild.kotlin

import com.ivieleague.kbuild.common.HasLibraries
import com.ivieleague.kbuild.common.HasSourceRoots
import com.ivieleague.kbuild.common.Module
import com.ivieleague.kbuild.jvm.HasJvmClassPaths
import com.ivieleague.kbuild.jvm.Jar
import org.jetbrains.kotlin.build.DEFAULT_KOTLIN_SOURCE_FILES_EXTENSIONS
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.config.IncrementalCompilation
import org.jetbrains.kotlin.incremental.EmptyICReporter
import org.jetbrains.kotlin.incremental.IncrementalJvmCompilerRunner
import org.jetbrains.kotlin.incremental.classpathAsList
import org.jetbrains.kotlin.incremental.multiproject.EmptyModulesApiHistory
import java.io.File

interface KotlinJVMModule : Module, HasSourceRoots, HasLibraries, HasJvmClassPaths {
    val buildFolder: File get() = root.resolve("build/kotlin").also { it.parentFile.mkdirs() }
    val kotlinClasspathOutput: File get() = root.resolve("out/kotlinClasspath")
    val kotlinJarOutput: File get() = root.resolve("out/kotlin.jar")

    override val jvmClassPaths: List<File>
        get() = listOf(buildKotlin()) + libraries.map { it.default }

    private val allKotlinSourceFiles get() = sourceRoots.asSequence().flatMap { it.walkTopDown() }.filter { it.extension == "kt" }.toList()

    fun kotlinArguments(arguments: K2JVMCompilerArguments) {
        arguments.moduleName = this.name
        arguments.classpathAsList = libraries.map { it.default }
        arguments.freeArgs = allKotlinSourceFiles.map { it.toString() }.toList()
        arguments.noStdlib = true
        arguments.destination = kotlinClasspathOutput.toString()
    }


    fun buildKotlin(): File {
        IncrementalCompilation.setIsEnabledForJvm(true)
        setIdeaIoUseFallback()
        buildFolder.mkdirs()
        val collector = Kotlin.CompilationMessageCollector()
        val code = IncrementalJvmCompilerRunner(
            workingDir = File(buildFolder, "cache"),
            reporter = EmptyICReporter,
            usePreciseJavaTracking = true,
            outputFiles = emptyList(),
            buildHistoryFile = File(buildFolder, "build-history.bin"),
            modulesApiHistory = EmptyModulesApiHistory,
            kotlinSourceFilesExtensions = DEFAULT_KOTLIN_SOURCE_FILES_EXTENSIONS
        ).compile(
            allSourceFiles = allKotlinSourceFiles.toList(),
            args = K2JVMCompilerArguments().also(this::kotlinArguments),
            messageCollector = collector,
            providedChangedFiles = null
        )
        for (message in collector.messages) {
            if (message.severity <= CompilerMessageSeverity.WARNING) {
                println("${message.message} at ${message.location}")
            }
        }
        if (code != ExitCode.OK) {
            throw Kotlin.CompilationException(collector.messages)
        }
        kotlinClasspathOutput.resolve("META-INF").also { it.mkdirs() }.resolve("MANIFEST.MF").takeIf { !it.exists() }
            ?.outputStream()?.buffered()?.use {
                Jar.defaultManifest().write(it)
            }
        Jar.create(kotlinClasspathOutput, kotlinJarOutput)
        return kotlinJarOutput
    }

    //TODO: Potential extension: call Java compiler afterwards to handle .java files
}