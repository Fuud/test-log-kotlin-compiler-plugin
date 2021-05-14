package com.github.fuud.test.logger.plugin

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.intellij.lang.annotations.Language
import org.junit.Test
import org.junit.internal.requests.ClassRequest
import org.junit.internal.runners.JUnit4ClassRunner
import org.junit.runner.Description
import org.junit.runner.Result
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener
import org.junit.runner.notification.RunNotifier
import org.junit.runners.JUnit4
import java.lang.reflect.InvocationTargetException
import kotlin.test.assertEquals
import kotlin.test.fail

class Test1 {

    @Test
    fun test() {
        executeTest(
            """
            println("Hello")
            (1..5).forEach { index ->
              println(index)
            }
            println("Hello")
        """.trimIndent()
        )
    }
}

fun executeTest(
    @Language(
        value = "kotlin", prefix = """
     import org.junit.Test
 class MyTest{
    @Test
    fun test(){
""", suffix = "}}"
    ) testBody: String
) {
    val source = """
            import org.junit.Test
            class MyTest{
              @Test
              fun test(){
                 $testBody
              }
            }
            """.trimIndent()

    val result = compile(
        listOf(SourceFile.kotlin("main.kt", source, trimIndent = false))
    )
    assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

    val clazz = result.classLoader.loadClass("MyTest")
    ClassRequest(clazz).runner.run(RunNotifier().apply {
        addListener(object : RunListener() {
            override fun testRunStarted(description: Description?) {
                super.testRunStarted(description)
            }

            override fun testRunFinished(result: Result?) {
                super.testRunFinished(result)
            }

            override fun testStarted(description: Description?) {
                super.testStarted(description)
            }

            override fun testFinished(description: Description?) {
                super.testFinished(description)
            }

            override fun testFailure(failure: Failure?) {
                super.testFailure(failure)
            }
        })
    })
}

fun compile(
    list: List<SourceFile>
): KotlinCompilation.Result {
    return KotlinCompilation().apply {
        sources = list
        useIR = true
        messageOutputStream = System.out
        compilerPlugins = listOf(TestLoggerComponentRegistrar())
        inheritClassPath = true
    }.compile()
}
