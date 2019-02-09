package com.ucu

import java.io.Serializable

sealed class Message: Serializable

class Ping: Message()


class Request(val id: String): Message()
class Response(val id: String): Message()

