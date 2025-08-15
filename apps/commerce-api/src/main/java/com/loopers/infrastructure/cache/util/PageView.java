package com.loopers.infrastructure.cache.util;

import com.loopers.application.product.ProductInfo;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

public class PageView<T> {

    public List<T> content;
    public int page;
    public int size;
    public long total;

    public PageView() {
    }

    public PageView(List<T> content, int page, int size, long total) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.total = total;
    }

    public static PageView<ProductInfo> from(Page<ProductInfo> result) {
        return new PageView<>(
            result.getContent(),
            result.getNumber(),
            result.getSize(),
            result.getTotalElements()
        );
    }

    public Page<T> toPage() {
        return new PageImpl<>(
            content,
            PageRequest.of(page, size),
            total
        );
    }

}
