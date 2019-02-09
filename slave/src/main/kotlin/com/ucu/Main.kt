package com.ucu

import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

object Main {


    val data: Map<String, String> = mapOf(
            "Sasha" to "Hello",
            "Ivan" to "Hello too")

    val tmpFolder = File("/Users/sasha/programming/map-reduce/tmp/")

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
                is Request -> {
                    val result = handleRequest(message)
                    output.writeObject(result)
                }
            }
        }
    }

    fun handleRequest(req: Request): Response {
        val tmpFile = File.createTempFile("slave", req.id, tmpFolder).also { it.deleteOnExit() }
        tmpFile.writeBytes(req.code)
        val mapper = CodeLoader(tmpFile).loadMapper<String, String, Int>(req.mapper)!!
        val mapped = data.map { (k, v) -> mapper.map(k, v) }.toMap()
        tmpFile.delete()
        return Response(req.id, mapped)
    }

}