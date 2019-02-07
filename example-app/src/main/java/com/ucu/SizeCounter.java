package com.ucu;

import kotlin.Pair;
import org.jetbrains.annotations.NotNull;

public class SizeCounter implements Mapper<String, String, Integer> {

    @NotNull
    @Override
    public Pair<String, Integer> map(String key, String value) {
        return new Pair<>(key, value.length());
    }
}
