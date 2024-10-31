package com.webapp.service;

import com.timgroup.statsd.StatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClient;

public class MetricService {
    private final StatsDClient statsd = new NonBlockingStatsDClient("prefix", "localhost", 8125);

    public void recordApiCall(String apiName) {
        statsd.incrementCounter(apiName + ".call.count");
    }

    public void recordApiDuration(String apiName, long durationMillis) {
        statsd.recordExecutionTime(apiName + ".duration", durationMillis);
    }

    public void recordDbQueryDuration(long durationMillis) {
        statsd.recordExecutionTime("db.query.duration", durationMillis);
    }

    public void recordS3CallDuration(long durationMillis) {
        statsd.recordExecutionTime("s3.call.duration", durationMillis);
    }
}
