package com.loopers.infrastructure.cache.core;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import lombok.Getter;


@Getter
public abstract class TypeRef<T> {
    private final Type type;
    
    protected TypeRef() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType p) {
            this.type = p.getActualTypeArguments()[0];
        } else {
            throw new IllegalStateException("TypeRef must be used with type parameter");
        }
    }


}
