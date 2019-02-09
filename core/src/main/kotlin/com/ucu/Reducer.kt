package com.ucu

interface Reducer<K, V, R> {
    fun reduce(key: K, values: List<V> ): List<R>
}