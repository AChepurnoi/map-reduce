package com.ucu

import java.io.Serializable

sealed class Message: Serializable

class Ping: Message()


class Request(val id: String, val code: ByteArray, val mapper: String): Message()
class Response(val id: String, val data: Map<String, Int>): Message()

