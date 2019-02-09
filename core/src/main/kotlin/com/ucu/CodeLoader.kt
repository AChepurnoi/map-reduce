package com.ucu

import java.io.File
import java.net.URLClassLoader

class CodeLoader(private val file: File) {

    private val url = file.toURI().toURL()
    private val loader = URLClassLoader(arrayOf(url))


    @Suppress("UNCHECKED_CAST")
    fun <K, V, O> loadMapper(fullClassName: String): Mapper<K, V, O>? {
        val instance = loadClass(fullClassName)
        return instance as? Mapper<K, V, O>
    }

    private fun loadClass(className: String): Any? {
        val result = kotlin.runCatching { loader.loadClass(className) }
        if (result.isFailure) return null
        val clazz = result.getOrThrow()
        return clazz.getConstructor().newInstance()
    }

    @Suppress("UNCHECKED_CAST")
    fun <K, V, R> loadReducer(fullClassName: String): Reducer<K, V, R>? {
        val instance = loadClass(fullClassName)
        return instance as? Reducer<K, V, R>
    }
}