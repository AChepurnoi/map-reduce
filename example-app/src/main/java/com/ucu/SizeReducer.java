package com.ucu;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class SizeReducer implements Reducer<String, Integer, Integer> {

    @NotNull
    @Override
    public List<Integer> reduce(String key, @NotNull List<? extends Integer> values) {
       return  Collections.singletonList(values.stream().mapToInt(x -> x).sum());
    }
}
