package com.ucu

import java.io.File
import java.net.Socket

object Main {
    val tmpFolder = File(Configuration.tmpFolder)

    @JvmStatic
    fun main(args: Array<String>) {
        val socket = Socket(Configuration.host, Configuration.port).also { it.soTimeout = 500 }
        val slave = Slave(socket)
        slave.listener()
        println("-----[Finished]-----")

    }


}