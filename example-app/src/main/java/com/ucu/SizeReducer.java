package com.ucu;

import kotlin.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SizeReducer implements Reducer<String, Integer, Integer> {

    @NotNull
    @Override
    public Pair<String, Integer> reduce(String key, @NotNull List<? extends Integer> values) {
       return new Pair<>(key, values.stream().mapToInt(x -> x).sum());
    }
}
