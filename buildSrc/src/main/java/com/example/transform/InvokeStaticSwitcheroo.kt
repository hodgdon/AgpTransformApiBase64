package com.example.transform

import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.ASM4
import org.objectweb.asm.Opcodes.ASM5
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.logging.Level.SEVERE
import java.util.logging.Logger
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream


class InvokeStaticSwitcheroo(private val replacements : List<Pair<StaticMethodReplacement, StaticMethodReplacement>>) {
    data class StaticMethodReplacement(
        val owner : String,
        val name : String,
        val descriptor : String
    )

    private inline fun transformStaticInvoke(
        opcode : Int,
        owner: String?,
        name: String?,
        descriptor: String?,
        isInterface: Boolean,
        body: (opcode : Int, name: String?, owner: String?, descriptor: String?, isInterface : Boolean) -> Unit
    ) : Boolean {
        return when(opcode) {
            Opcodes.INVOKESTATIC -> {
                val replacement = replacements.firstOrNull { replacement ->
                    replacement.first.name == name && replacement.first.owner == owner && replacement.first.descriptor == descriptor
                }?.second
                if(replacement != null) true.also {
                    body(opcode, replacement.name, replacement.owner, replacement.descriptor, isInterface)
                } else false
            }
            else -> false
        }
    }


    private inner class MethodExchangeClassVisitor(private val classVisitor: ClassVisitor) : ClassVisitor(ASM4, classVisitor) {
        override fun visitMethod(
            access: Int,
            name: String?,
            descriptor: String?,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor {
            return MethodExchangeMethodVisitor(classVisitor.visitMethod(access, name, descriptor, signature, exceptions))
        }
    }

    private inner class MethodExchangeMethodVisitor(methodVisitor: MethodVisitor) : MethodVisitor(ASM5, methodVisitor) {
        override fun visitMethodInsn(
            opcode: Int,
            owner: String?,
            name: String?,
            descriptor: String?,
            isInterface: Boolean
        ) {
            if (!transformStaticInvoke(
                    opcode=opcode,
                    owner = owner,
                    name = name,
                    descriptor = descriptor,
                    isInterface = isInterface
                ) { updatedOpcode, updatedName, updatedOwner, updatedDescriptor, updatedIsInterface ->
                    visitMethodInsn(updatedOpcode, updatedOwner, updatedName, updatedDescriptor, updatedIsInterface)
                }
            ) {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
            }
        }
    }

    fun transform(content: ByteArray): ByteArray? {
        return try {
            val classReader = ClassReader(content)
            val classWriter = ClassWriter(classReader, 0)
            val adapter = MethodExchangeClassVisitor(classWriter)
            classReader.accept(adapter, 0)
            classWriter.toByteArray()
        } catch (e: IOException) {
            Logger.getLogger(InvokeStaticSwitcheroo::class.java.name).log(SEVERE, null, e)
            null
        }
    }

    private fun transform(inputEntry : ZipEntry, content : ByteArray) : ByteArray {
        return if(inputEntry.name.endsWith(".class")) {
            transform(content)!!
        } else content
    }

    fun transformZip(inputFile : ZipFile, outputFile : File) {
        ZipOutputStream(FileOutputStream(outputFile)).use { zipOutputStream ->
            for(entry: ZipEntry in inputFile.entries()) {
                val content = inputFile.getInputStream(entry).use { it.readBytes() }
                val transformedContent = transform(entry, content)
                zipOutputStream.putNextEntry(ZipEntry(entry).apply {
                    size = transformedContent.size.toLong()
                    compressedSize = -1
                })
                zipOutputStream.write(transformedContent)
                zipOutputStream.closeEntry()
            }
        }
    }
}