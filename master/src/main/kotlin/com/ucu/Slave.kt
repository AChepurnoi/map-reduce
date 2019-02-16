package com.ucu

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger


@ObsoleteCoroutinesApi
@UseExperimental(ExperimentalCoroutinesApi::class)
class Slave(s: Socket) {
    private val socket = s.apply { soTimeout = 100 }
    private val out = ObjectOutputStream(socket.getOutputStream()).also { it.flush() }
    private val input = ObjectInputStream(socket.getInputStream())
    private val outputChannel = Channel<Message>()
    private val mailbox = ConflatedBroadcastChannel<Message>()
    private inline fun <reified T> subscribeTo() = mailbox.openSubscription().filter { it is T }.map { it as T }

    private var alive = true
    fun isAlive() = alive

    private val sender = GlobalScope.launch {
        while (socket.isConnected && alive) {
            val message = outputChannel.receive()
            kotlin.runCatching { out.writeObject(message) }
                    .onFailure { terminate() }
        }
    }

    private val listener: Job = GlobalScope.launch {
        while (socket.isConnected && alive) {
            delay(100)
            kotlin.runCatching { input.readObject() as Message }
                    .onSuccess { mailbox.send(it) }
                    .onFailure { println("[Listener]: Error $it") }
        }
    }

    private val heartbeat: Job = GlobalScope.launch {
        while (socket.isConnected && alive) {
            delay(100)
            outputChannel.send(Ping())
        }
    }

    fun terminate() {
        kotlin.runCatching { listener.cancel() }
        kotlin.runCatching { heartbeat.cancel() }
        kotlin.runCatching { sender.cancel() }
        kotlin.runCatching { input.close() }
        kotlin.runCatching { out.close() }
        kotlin.runCatching { socket.close() }
        alive = false
        println("[Slave]: Slave is terminated")
    }

    suspend fun <K, V> map(request: Request): Result<Response<K, V>> {
        if (!alive) return Result.failure(RuntimeException("Slave is dead"))
        println("[SlaveMap]: Mapping ${request.id}")
        val response = withTimeout(5000) {
            outputChannel.send(request)
            subscribeTo<Response<K, V>>().filter { it.id == request.id }.first()
        }
        return Result.success(response)

    }
}