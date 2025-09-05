package io.accelerate.track.code.time;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeUnit;

public interface TimeSource {

    long currentTimeNano();

    void wakeUpAt(long timestamp, TimeUnit timeUnit) throws InterruptedException, BrokenBarrierException;

    void wakeUpNow();
}
