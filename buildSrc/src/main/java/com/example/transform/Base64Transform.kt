package com.example.transform

import com.android.build.api.transform.*
import java.io.File
import java.util.zip.ZipFile


class Base64Transform : Transform() {
    override fun getName(): String = "Base64Transform"

    override fun getInputTypes(): Set<QualifiedContent.ContentType> = setOf(QualifiedContent.DefaultContentType.CLASSES)

    override fun isIncremental(): Boolean = false

    override fun getScopes(): MutableSet<QualifiedContent.Scope> = mutableSetOf(QualifiedContent.Scope.SUB_PROJECTS)

    override fun transform(invocation: TransformInvocation) {
        val methodExchangeUtil = InvokeStaticSwitcheroo(
            listOf(
                // INVOKESTATIC org/apache/commons/codec/binary/Base64.encodeBase64String ([B)Ljava/lang/String;
                // to
                // INVOKESTATIC com/example/lib/OldApacheCommonCodecCompatKt.encodeBase64StringCompat ([B)Ljava/lang/String;
                InvokeStaticSwitcheroo.StaticMethodReplacement(
                    "org/apache/commons/codec/binary/Base64",
                    "encodeBase64String",
                    "([B)Ljava/lang/String;"
                ) to InvokeStaticSwitcheroo.StaticMethodReplacement(
                    "com/example/lib/OldApacheCommonCodecCompatKt",
                    "encodeBase64StringCompat",
                    "([B)Ljava/lang/String;"
                )
            )
        )

        if (!invocation.isIncremental) {
            invocation.outputProvider.deleteAll()
        }

        val inputs = invocation.inputs.flatMap { it.jarInputs + it.directoryInputs }
        inputs.forEach { input: QualifiedContent ->
            val format: Format = when (input) {
                is JarInput -> Format.JAR
                else -> Format.DIRECTORY
            }
            val contentLocation: File = invocation.outputProvider.getContentLocation(
                input.name,
                input.contentTypes,
                input.scopes,
                format
            )

            methodExchangeUtil.transformZip(
                inputFile = ZipFile(input.file),
                outputFile = contentLocation)
        }
    }
}