package com.github.fuud.test.logger.plugin

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.builtins.DefaultBuiltIns
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irCallOp
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockBodyImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeBuilder
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.impl.buildSimpleType
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.name.FqName

class TestLoggerCallTransformer(
    private val file: IrFile,
    private val fileSource: String,
    private val context: IrPluginContext,
    private val messageCollector: MessageCollector,
) : IrElementTransformerVoidWithContext() {
    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
        if (declaration.annotations.any { it.type.classFqName.toString() == "org.junit.Test" }) {
            val symbol = currentScope!!.scope.scopeOwnerSymbol
            val builder = DeclarationIrBuilder(context, symbol, declaration.startOffset, declaration.endOffset)

            val printlnCall = builder.irCall(
                this.context.referenceFunctions(FqName("kotlin.io.println")).single { it.toString().contains("Any?") },
                IrSimpleTypeBuilder().apply {
                    classifier = context.referenceClass(FqName(Unit::class.qualifiedName!!))
                }
                    .buildSimpleType(),
                1,
                0
            )
            val body = declaration.body!!
            declaration.body = IrBlockBodyImpl(body.startOffset, body.endOffset).apply {
                body.statements.forEach { statement ->
                    statements.add(printlnCall.deepCopyWithSymbols().apply {
                        putValueArgument(0, builder.irString(statementText(statement)))
                    })
                    statements.add(statement)
                }
            }

        }
        return declaration
    }

    private fun statementText(statement: IrStatement): String {
        return fileSource.substring(statement.startOffset..statement.endOffset).prependIndent(">>> ")
    }
}