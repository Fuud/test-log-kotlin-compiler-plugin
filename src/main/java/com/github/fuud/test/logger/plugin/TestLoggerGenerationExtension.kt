package com.github.fuud.test.logger.plugin

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.path
import java.io.File

class TestLoggerGenerationExtension(private val messageCollector: MessageCollector) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        for (file in moduleFragment.files) {
            val fileSource = File(file.path).readText()
                .replace("\r\n", "\n") // https://youtrack.jetbrains.com/issue/KT-41888

            TestLoggerCallTransformer(file, fileSource, pluginContext, messageCollector)
                .visitFile(file)
        }
    }
}