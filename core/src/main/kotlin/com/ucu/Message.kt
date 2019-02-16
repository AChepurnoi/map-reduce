package com.ucu

import java.io.Serializable

sealed class Message: Serializable

class Ping: Message()


class Request(val id: String, val code: ByteArray, val mapper: String): Message()
class Response<K, V>(val id: String, val data: Map<K, V>): Message()

