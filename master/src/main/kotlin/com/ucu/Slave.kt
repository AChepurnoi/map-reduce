package com.ucu

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger


class Slave(val socket: Socket) {

    private val log = Logger.getLogger(javaClass.name)
    private val out = ObjectOutputStream(socket.getOutputStream()).also { it.flush() }
    private val input = ObjectInputStream(socket.getInputStream())
    private val response: MutableMap<String, Response> = ConcurrentHashMap()
    var alive = true
        private set

    init {
        socket.soTimeout = 100
    }

    private var listener: Job = GlobalScope.launch { listener() }
    private var heartbeat: Job = GlobalScope.launch { heartbeat() }


    private suspend fun listener() {
        while (socket.isConnected && alive) {
            delay(500)
            kotlin.runCatching { input.readObject() as? Message }.getOrNull()
                    ?.let { handle(it) }
        }
    }

    private suspend fun heartbeat() {
        while (socket.isConnected && alive) {
            delay(500)
            kotlin.runCatching { out.writeObject(Ping()) }
                    .onFailure { terminate() }
        }
    }

    private fun handle(message: Message) {
        when (message) {
            is Ping -> {
                log.info("[Handle]: Ping")
            }
            is Response -> {
                response[message.id] = message
            }
        }
    }

    private fun terminate() {
        log.info("[Slave]: Terminating slave")
        kotlin.runCatching { listener.cancel() }
        kotlin.runCatching { heartbeat.cancel() }
        kotlin.runCatching { input.close() }
        kotlin.runCatching { out.close() }
        kotlin.runCatching { socket.close() }
        alive = false
    }

    suspend fun map(id: String): Response {
        log.info("[Test]: Mapping $id")
        kotlin.runCatching {
            out.writeObject(Request(id))
        }.onFailure { terminate() }

        while (!response.containsKey(id) && alive) {
            delay(250)
        }

        return response.remove(id) ?: throw RuntimeException("Internal error")


    }
}