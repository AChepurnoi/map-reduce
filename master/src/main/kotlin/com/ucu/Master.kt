package com.ucu

import kotlinx.coroutines.*
import java.io.File
import java.net.ServerSocket
import java.util.*


@ObsoleteCoroutinesApi
class Master(port: Int) {

    private val tmpFolder = File(Configuration.tmpFolder)

    private var running = true
    private val server = ServerSocket(port).apply { soTimeout = 200 }

    private var slaves: MutableList<Slave> = mutableListOf()
    private var slaveMonitor: Job = GlobalScope.launch {
        while (running) {
            delay(1000)
            println("[Watch]: Connected: ${slaves.size} | Alive: ${slaves.filter { it.isAlive() }.size}")
//            @TODO temporary dead slaves filtering
            synchronized(slaves) {
                slaves.filterNot { it.isAlive() }
                        .forEach { slaves.remove(it) }
            }
        }
    }


    suspend fun <K, R> mapReduce(mapperName: String, reducerName: String, code: ByteArray): Result<Map<K, R>> {
        if (slaves.isEmpty()) return Result.success(emptyMap())

        val id = UUID.randomUUID().toString()

        val reducer = loadReducer<K, R>(code, reducerName)

        val mapResults = slaves
                .map { GlobalScope.async { it.map<K, R>(Request(id, code, mapperName)) } }
                .map { it.await() }.toList()

        if (mapResults.any { it.isFailure }) {
            println("[MapReduce]: One or more node failed to handle request")
            return Result.failure(RuntimeException("One or more nodes failed to process request"))
        }

        val res = mapResults
                .map { it.getOrThrow() }
                .flatMap { it.data.toList() }
                .groupBy({ it.first }, { it.second })
                .map { reducer.reduce(it.key, it.value) }
                .toMap()

        return Result.success(res)

    }

    private fun <K, R> loadReducer(code: ByteArray, reducerName: String): Reducer<K, R, R> {
        val jarFile = File.createTempFile("master", UUID.randomUUID().toString(), tmpFolder).also {
            it.deleteOnExit()
            it.writeBytes(code)
        }
        val reducer = CodeLoader(jarFile).loadReducer<K, R, R>(reducerName)!!
        jarFile.delete()
        return reducer
    }

    suspend fun listen() {
        while (running) {
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
        slaveMonitor.cancel()
        server.close()
        slaves.forEach { it.terminate() }
    }
}

