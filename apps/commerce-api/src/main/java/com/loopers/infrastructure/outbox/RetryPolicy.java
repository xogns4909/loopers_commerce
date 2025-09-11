package com.loopers.infrastructure.outbox;

public interface RetryPolicy {

    long backoffMillis(int attempt);
}

class DefaultRetryPolicy implements RetryPolicy {
    @Override
    public long backoffMillis(int attempt) {
        long v = (long) Math.pow(2, Math.max(1, attempt)) * 1000L;
        return Math.min(v, 30_000L);
    }
}
