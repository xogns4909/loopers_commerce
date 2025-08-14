package com.loopers.infrastructure.cache.redis.codec;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;


public class JacksonCodec<V> implements ValueCodec<V> {

    private final ObjectMapper om;
    private final JavaType type;

    public JacksonCodec(ObjectMapper om, TypeReference<V> ref) {
        this.om = om;
        this.type = om.getTypeFactory().constructType(ref);
    }

    @Override
    public String serialize(V value) throws Exception {
        return om.writeValueAsString(value);
    }

    @Override
    public V deserialize(String raw) throws Exception {
        if (raw == null || "null".equals(raw)) {
            return null;
        }
        return om.readValue(raw, type);
    }
}
