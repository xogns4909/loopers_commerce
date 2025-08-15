package com.loopers.infrastructure.cache.util;

import java.util.Map;
import java.util.stream.Collectors;

public final class KeyBuilder {
    private KeyBuilder(){}
    
    public static String build(String prefix, String ns, long version, Map<String,?> params) {
        String canon = (params == null || params.isEmpty())
            ? "all"
            : params.entrySet().stream()
                .filter(e -> e.getValue()!=null && !String.valueOf(e.getValue()).isBlank())
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey()+"="+e.getValue())
                .collect(Collectors.joining("&"));
        return "%s:%s:v%d:%s".formatted(prefix, ns, version, canon);
    }
}
