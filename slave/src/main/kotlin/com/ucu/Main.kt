package com.ucu

import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val socket = Socket("127.0.0.1", 12000)
        val output = ObjectOutputStream(socket.getOutputStream()).also { it.flush() }
        val input = ObjectInputStream(socket.getInputStream())
        output.flush()

        socket.soTimeout = 500

        while (socket.isConnected) {
            val message = kotlin.runCatching { (input.readObject() as? Message) }.getOrNull()
            println("[Listener]: I got $message")
            when (message) {
                is Ping -> output.writeObject(Ping())
                is Request -> output.writeObject(Response(message.id))
            }
        }

    }

}