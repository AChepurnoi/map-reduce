package com.ucu

object Configuration {

    val env = System.getenv()!!
    val tmpFolder: String = env.getOrDefault("TMP_FOLDER", "/tmp")
    val host: String = env.getOrDefault("MASTER_HOST", "127.0.0.1")
    val port: Int = env.getOrDefault("MASTER_PORT", "12000").toInt()

    val data: Map<String, String> = env.getOrDefault("NODE_DATA", "SASHA:TEXT_ONE|IVAN:TEXT_TWO")
            .let {data -> data.split("|").map { it.split(":") }.map { it[0] to it[1] }.toMap() }

}