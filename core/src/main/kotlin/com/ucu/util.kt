package com.ucu

data class Failure(val ex: Exception)
data class Result<S>(val success: S?, val failure: Failure?) {
    val isSuccessful = success != null
    val isFailed = failure != null

    fun data() = success!!
    fun exception() = failure!!.ex

    suspend fun onSuccess(block: suspend (S) -> Unit) = success?.let { block(success) }

}


fun <T> Try(block: () -> T): Result<T> {
    return try {
        Result(block(), null)
    } catch (e: Exception) {
        Result(null, Failure(e))
    }
}



