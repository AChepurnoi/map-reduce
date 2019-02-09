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


class Slave(s: Socket) {

    private val socket = s.apply {
        soTimeout = 100
    }
    private val log = Logger.getLogger(javaClass.name)
    private val out = ObjectOutputStream(socket.getOutputStream()).also { it.flush() }
    private val input = ObjectInputStream(socket.getInputStream())
    private val response: MutableMap<String, Response> = ConcurrentHashMap()
    public var alive = true
        private set


    private var listener: Job = GlobalScope.launch { listener() }
    private var heartbeat: Job = GlobalScope.launch { heartbeat() }


    private suspend fun listener() {
        while (socket.isConnected && alive) {
            delay(100)
            kotlin.runCatching { input.readObject() as? Message }.getOrNull()
                    ?.let { handle(it) }
        }
    }

    private suspend fun heartbeat() {
        while (socket.isConnected && alive) {
            delay(100)
            kotlin.runCatching { out.writeObject(Ping()) }
                    .onFailure { terminate() }
        }
    }

    private fun handle(message: Message) {
        when (message) {
            is Ping -> {
//                log.info("[Handle]: Ping received")
            }
            is Response -> {
                response[message.id] = message
            }
        }
    }

    private fun terminate() {
        kotlin.runCatching { listener.cancel() }
        kotlin.runCatching { heartbeat.cancel() }
        kotlin.runCatching { input.close() }
        kotlin.runCatching { out.close() }
        kotlin.runCatching { socket.close() }
        alive = false
        println("[Slave]: Error happened. Slave is terminated")
    }

    suspend fun map(request: Request): Result<Response> {

        log.info("[Test]: Mapping ${request.id}")
        val result = kotlin.runCatching { out.writeObject(request) }.onFailure { terminate() }

        if (result.isFailure) return Result.failure(result.exceptionOrNull()!!)

//        @TODO possible inf loop here
        while (!response.containsKey(request.id) && alive) {
            delay(250)
        }

        return kotlin.runCatching {
            response.remove(request.id) ?: throw RuntimeException("Intenal error. No response")
        }

    }
}