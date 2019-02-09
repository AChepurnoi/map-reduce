package com.ucu


//abstract class MessageHandler {
//    var next: MessageHandler? = null
//
//    protected abstract fun handle(message: Message): Boolean
//
//    fun process(message: Message): Boolean {
//        val result = handle(message)
//        return result || (next?.process(message) ?: false)
//    }
//
//}
//
//class PingMessageHandler : MessageHandler() {
//    override fun handle(message: Message): Boolean = (message as? Ping)?.run {
//        println("Ping recieved")
//        return@run true
//    } ?: false
//
//}