package com.ucu

import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.ObjectInputStream
import java.net.ServerSocket


object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        println("Running master")

//        val file = File("/Users/sasha/programming/map-reduce/example-app/build/classes/java/main/")
//        val jar = File("/Users/sasha/programming/map-reduce/example-app/build/libs/example-app-1.jar")
//
//        val mapper = CodeLoader(jar).loadMapper<String, String, Int>("com.ucu.SizeCounter")
//        val result = mapper?.map("key", "long text")


        val server = SlaveListener(12000)
        println("Waiting for connection")
        runBlocking {
            server.listen()
        }
        println("Finished")
//        val reader = ObjectInputStream(socket.getInputStream())
//
//        reader.readObject()

//        println(result)
    }
}