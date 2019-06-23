package com.example.transform

import okio.buffer
import okio.source
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.zip.ZipFile


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class InvokeStaticSwitcherooTest {
    private lateinit var invokeStaticSwitcheroo: InvokeStaticSwitcheroo

    @BeforeAll
    internal fun setUp() {
        invokeStaticSwitcheroo = InvokeStaticSwitcheroo(
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
    }

    @Test
    internal fun whenConversionRequired_contentChanges() {
        val inputStream = javaClass.getResourceAsStream("UsesApacheCommonCodecKt1.class")
        val inputContent = inputStream.source().buffer().readByteArray()
        val outputContent = invokeStaticSwitcheroo.transform(inputContent)
        assertNotNull(outputContent)
        assertFalse(outputContent!! contentEquals inputContent)
    }

    @Test
    internal fun twiceBake() {
        val inputStream = javaClass.getResourceAsStream("UsesApacheCommonCodecKt1.class")
        val inputContent = invokeStaticSwitcheroo.transform(inputStream.source().buffer().readByteArray())
        val outputContent = invokeStaticSwitcheroo.transform(inputContent!!)
        assertNotNull(outputContent)
        assertTrue(outputContent!! contentEquals inputContent)
    }

    @Test
    internal fun whenConversionNotRequired_contentRemainsSame() {
        val inputStream = javaClass.getResourceAsStream("UsesApacheCommonCodecKt2.class")
        val inputContent = inputStream.source().buffer().readByteArray()
        val outputContent = invokeStaticSwitcheroo.transform(inputContent)
        assertNotNull(outputContent)
        assertTrue(outputContent!! contentEquals inputContent)
    }

    @Test
    internal fun whenConversionRequiredOnZipFile_fileChanges(@TempDir tempDir : File) {
        val outputFile = File(tempDir, "output.jar")
        val inputFile = File(javaClass.getResource("lib.jar").file)
        invokeStaticSwitcheroo.transformZip(ZipFile(inputFile), outputFile)
        assertTrue(outputFile.exists())
        assertFalse(outputFile.readBytes() contentEquals inputFile.readBytes())
    }


    @Test
    internal fun twiceBakeZip(@TempDir tempDir : File) {
        val firstOutputFile = File(tempDir, "output1.jar")
        val secondOutputFile = File(tempDir, "output2.jar")
        val inputFile = File(javaClass.getResource("lib.jar").file)
        invokeStaticSwitcheroo.transformZip(ZipFile(inputFile), firstOutputFile)
        invokeStaticSwitcheroo.transformZip(ZipFile(firstOutputFile), secondOutputFile)
        assertTrue(secondOutputFile.exists())
        assertArrayEquals(firstOutputFile.readBytes(), secondOutputFile.readBytes())
    }
}