package com.ucu

data class Failure(val ex: RuntimeException)
data class Result<S>(val success: S?, val failure: Failure?) {
    val isSuccessful = success != null
    val isFailed = failure != null

    fun data() = success!!
    fun exception() = failure!!.ex

}


fun <T> Try(block: () -> T): Result<T> {
    return try {
        Result(block(), null)
    } catch (e: RuntimeException) {
        Result(null, Failure(e))
    }
}



