package com.ucu

import kotlinx.coroutines.*
import java.io.File
import java.net.ServerSocket
import java.util.*


class Master(port: Int) {

    val tmpFolder = File("/Users/sasha/programming/map-reduce/tmp/")

    private var running = true
    private val server = ServerSocket(port).apply {
        soTimeout = 200
    }
    private var slaves: MutableList<Slave> = mutableListOf()
    private var slaveChecker: Job = GlobalScope.launch { slaveMonitor() }


    private suspend fun slaveMonitor() {
        while (running) {
            delay(5000)
            println("[Watch]: Connected: ${slaves.size} | Alive: ${slaves.filter { it.alive }.size}")
//            @TODO replace it
            synchronized(slaves) {
                slaves.filterNot { it.alive }
                        .forEach { slaves.remove(it) }
            }
        }
    }


    suspend fun map(code: ByteArray) {
        if (slaves.isEmpty()) return
        val id = UUID.randomUUID().toString()

        val jarFile = File.createTempFile("master", id, tmpFolder).also {
            it.deleteOnExit()
            it.writeBytes(code)
        }
        val reducer = CodeLoader(jarFile).loadReducer<String, Int, Int>("com.ucu.SizeReducer")!!
        val results = slaves
                .map { GlobalScope.async { it.map(Request(id, code, "com.ucu.SizeCounter")) } }
                .map { it.await() }.toList()

        if (results.any { it.isFailure }) {
            println("One or more node failed to handle request")
        }

        val groupped = results
                .map { it.getOrThrow() }
                .flatMap { it.data.toList() }
                .groupBy({ it.first }, { it.second })
                .toMap()

        val result = groupped.flatMap { reducer.reduce(it.key, it.value) }.toList()
        println(result)

        jarFile.delete()

    }

    suspend fun listen() {
        while (running) {
//            println("[Listen]: Trying to accept connection")
            delay(50)
            kotlin.runCatching { server.accept() }.onSuccess {
                val slave = Slave(it)
                println("[Listen]: Slave connected")
                synchronized(slaves) {
                    slaves.add(slave)
                }
            }
        }
    }

    fun stop() {
        running = false
        slaveChecker.cancel()
        server.close()
    }
}

