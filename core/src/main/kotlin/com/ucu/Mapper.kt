package com.ucu

interface Mapper<K, V, O> {
    fun map(key: K, value: V): Pair<K, O>
}