package com.loopers.application.cache.PageView;

import java.util.List;

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
}
