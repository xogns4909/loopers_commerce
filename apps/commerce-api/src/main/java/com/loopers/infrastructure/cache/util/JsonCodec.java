package com.loopers.infrastructure.cache.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.loopers.infrastructure.cache.core.TypeRef;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;


@Component
public class JsonCodec {

    private final ObjectMapper objectMapper;

    public JsonCodec(@Qualifier("cacheObjectMapper") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> String encode(T value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new CoreException(ErrorType.INTERNAL_ERROR,"인코딩 실패");
        }
    }

    public <T> T decode(String json, TypeRef<T> typeRef) {
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructType(typeRef.getType()));
        } catch (Exception e) {
            throw new CoreException(ErrorType.INTERNAL_ERROR,"디코딩 실패");
        }
    }
}
