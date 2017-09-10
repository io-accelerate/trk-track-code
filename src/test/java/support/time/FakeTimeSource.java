package support.time;

import tdl.record.sourcecode.time.TimeSource;

import java.util.concurrent.TimeUnit;

public class FakeTimeSource implements TimeSource {
    private long currentTimeNano;

    public FakeTimeSource() {
        currentTimeNano = 0;
    }

    @Override
    public long currentTimeNano() {
        currentTimeNano++;
        return currentTimeNano;
    }

    @Override
    public void wakeUpAt(long timestamp, TimeUnit timeUnit) throws InterruptedException {
        currentTimeNano = timeUnit.toNanos(timestamp);
    }
}
