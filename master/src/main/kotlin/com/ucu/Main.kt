package com.ucu

import java.io.File


object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        println("Running master")

        val file = File("/Users/sasha/programming/map-reduce/example-app/build/classes/java/main/")
        val mapper = CodeLoader(file).loadMapper<String, String, Int>("com.ucu.SizeCounter")
        val result = mapper?.map("key", "long text")
        println(result)
    }
}