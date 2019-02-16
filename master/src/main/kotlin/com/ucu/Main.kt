package com.ucu

import kotlinx.coroutines.*
import sun.misc.Signal
import java.io.File


@ObsoleteCoroutinesApi
object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        println("-------[Running master]-------")
        val server = Master(Configuration.port)
        val listener = GlobalScope.async { server.listen() }
        configureMapReduceSignalListener(server)
        configureShutdownListener(server)
        runBlocking {
            listener.await()
        }
    }

    private fun configureShutdownListener(server: Master) {
        Runtime.getRuntime().addShutdownHook(Thread {
            println("------[Stopping]------")
            server.stop()
        })
    }

    private fun configureMapReduceSignalListener(server: Master) {
        val jar = File(Configuration.jarPath)
        val data = jar.readBytes()
        Signal.handle(Signal("USR2")) {
            GlobalScope.launch {
                println("[MapReduce]: Starting map reduce job")
                server.mapReduce<String, Int>(Configuration.mapperName, Configuration.reducerName, data)
                        .onSuccess { println("[MapReduce]: Result $it") }
                        .onFailure { println("[MapReduce]: Error $it") }
            }
        }
    }
}