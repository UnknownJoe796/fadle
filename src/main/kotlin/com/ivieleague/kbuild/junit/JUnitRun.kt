package com.ivieleague.kbuild.junit

import com.ivieleague.kbuild.common.Producer
import com.ivieleague.kbuild.common.TestResult
import com.ivieleague.kbuild.grabStandardError
import com.ivieleague.kbuild.grabStandardOut
import com.ivieleague.kbuild.jvm.JVM
import com.ivieleague.kbuild.jvm.JankUntypedWrapper
import com.ivieleague.kbuild.jvm.untyped
import org.junit.platform.engine.DiscoverySelector
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.TestExecutionResult.Status
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.nio.charset.Charset
import java.util.*
import kotlin.jvm.optionals.getOrNull

class JUnitRun(
    val testModule: () -> File,
    val classpath: Producer<File>
) : Producer<TestResult>, (Set<String>) -> Set<TestResult> {
    val testClassNames: Set<String>
        get() {
            val testModuleResult = testModule()
            val loaded = JVM.load(this.classpath().toList() + testModuleResult)
            val annotationClass = loaded.loadClass("org.junit.jupiter.api.Test")
            val classes = JVM.listJavaClasses(testModuleResult)
            return classes
                .asSequence()
                .map { loaded.loadClass(it) }
                .filter { c ->
                    c.methods
                        .any { it.annotations.any { annotationClass.isInstance(it) } }
                }
                .map { it.name }
                .toSet()
        }

    override fun invoke(): Set<TestResult> = invoke {
        testClassNames.map { n ->
            DiscoverySelectors.selectClass(it, n)
        }
    }

    operator fun invoke(selectors: (JVM.JarFileLoader)->List<DiscoverySelector>): Set<TestResult> = buildSet {
        val loaded = JVM.load(this@JUnitRun.classpath().toList() + testModule())
        LauncherFactory.create().execute(
            LauncherDiscoveryRequestBuilder.request()
                .selectors(selectors(loaded))
                .build(),
            object : TestExecutionListener {
                override fun executionFinished(
                    testIdentifier: TestIdentifier,
                    testExecutionResult: TestExecutionResult
                ) {
                    add(
                        TestResult(
                            testIdentifier.displayName,
                            testExecutionResult.status == Status.SUCCESSFUL,
                            standardOutput = "",
                            standardError = "",
                            error = testExecutionResult.throwable.getOrNull()?.message,
                            durationSeconds = 1.0,
                            runAt = Date(),
                            runOn = "JUnit 5"
                        )
                    )
                }
            }
        )
    }

    override operator fun invoke(tests: Set<String>): Set<TestResult> = buildSet {
        val loaded = JVM.load(this@JUnitRun.classpath().toList() + testModule())
        LauncherFactory.create().execute(
            LauncherDiscoveryRequestBuilder.request()
                .selectors(tests.map { DiscoverySelectors.selectMethod(loaded, it) })
                .build(),
            object : TestExecutionListener {
                override fun executionFinished(
                    testIdentifier: TestIdentifier,
                    testExecutionResult: TestExecutionResult
                ) {
                    add(
                        TestResult(
                            testIdentifier.source.getOrNull()?.toString() ?: testIdentifier.displayName,
                            testExecutionResult.status == Status.SUCCESSFUL,
                            standardOutput = "",
                            standardError = "",
                            error = testExecutionResult.throwable.getOrNull()?.message,
                            durationSeconds = 1.0,
                            runAt = Date(),
                            runOn = "JUnit 5"
                        )
                    )
                }
            }
        )
    }

}