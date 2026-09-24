package com.vectordb.model;

import java.util.List;

@FunctionalInterface
public interface DistFn {
    float apply(List<Float> a, List<Float> b);
}
