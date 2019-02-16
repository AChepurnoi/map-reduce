package com.ucu

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

class Slave(private val socket: Socket) {
    private val output = ObjectOutputStream(socket.getOutputStream()).also { it.flush() }
    private val input = ObjectInputStream(socket.getInputStream())

    fun listener() {
        while (socket.isConnected) {
            val message = kotlin.runCatching { (input.readObject() as Message) }.getOrNull()
            when (message) {
                is Ping -> output.writeObject(Ping())
                is Request -> {
                    println("[Listener]: Doing map for ${message.id}")
                    val result = handleRequest(message)
                    output.writeObject(result)
                }
                null -> {
                    println("[Listener]: Null message. Master is terminated or failed to respond")
                    return
                }
            }
        }
    }

    private fun handleRequest(req: Request): Response<String, Int> {
        val tmpFile = File.createTempFile("slave", req.id, Main.tmpFolder).also { it.deleteOnExit() }
        tmpFile.writeBytes(req.code)
        val mapper = CodeLoader(tmpFile).loadMapper<String, String, Int>(req.mapper)!!
        val mapped = Configuration.data.map { (k, v) -> mapper.map(k, v) }.toMap()
        tmpFile.delete()
        return Response(req.id, mapped)
    }
}
