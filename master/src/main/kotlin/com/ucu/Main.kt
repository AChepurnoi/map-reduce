package com.ucu

import kotlinx.coroutines.*
import java.io.File


object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        println("Running master")

//        val file = File("/Users/sasha/programming/map-reduce/example-app/build/classes/java/main/")
        val jar = File("/Users/sasha/programming/map-reduce/example-app/build/libs/example-app-1.jar")
        val data = jar.readBytes()

        val server = Master(12000)
        println("Waiting for connection")

        val listener = GlobalScope.async {
            server.listen()
        }

        GlobalScope.launch {
            while (true) {
                delay(2000)
                server.map(data)
            }
        }

        runBlocking {
            listener.await()
        }
        println("Finished")
//        val reader = ObjectInputStream(socket.getInputStream())
//
//        reader.readObject()

//        println(result)
    }
}