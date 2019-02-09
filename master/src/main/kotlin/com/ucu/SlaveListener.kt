package com.ucu

import kotlinx.coroutines.*
import java.net.ServerSocket


class SlaveListener(val port: Int) {


    private var running = true
    private val server = ServerSocket(port).apply {
        soTimeout = 200
    }
    private var slaves: MutableList<Slave> = mutableListOf()
    private var slaveChecker: Job = GlobalScope.launch { slaveMonitor() }


    private suspend fun slaveMonitor() {
        while (running) {
            delay(5000)
            slaves.forEach { println("[Watch]: Slave alive=${it.alive}") }
            synchronized(slaves) {
                slaves.filterNot { it.alive }
                        .forEach { slaves.remove(it) }
            }
        }
    }


    suspend fun listen() {
        while (running) {
            println("[Listen]: Trying to accept connection")
            delay(50)
            kotlin.runCatching { server.accept() }.onSuccess {
                val slave = Slave(it)
                println("[Listen]: Slave created")
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

