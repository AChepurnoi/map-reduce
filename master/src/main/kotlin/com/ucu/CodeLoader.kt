package com.ucu

import java.io.File
import java.net.URLClassLoader

class CodeLoader(private val file: File) {

    private val url = file.toURI().toURL()
    private val loader = URLClassLoader(arrayOf(url))


    @Suppress("UNCHECKED_CAST")
    fun <K, V, O> loadMapper(fullClassName: String): Mapper<K, V, O>? {
        val result = Try { loader.loadClass(fullClassName) }
        if (result.isFailed) return null
        val clazz = result.data()
        val instance = clazz.getConstructor().newInstance()
        return instance as? Mapper<K, V, O>
    }
}