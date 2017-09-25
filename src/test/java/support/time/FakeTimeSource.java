package support.time;

import tdl.record.sourcecode.time.TimeSource;

import java.util.concurrent.TimeUnit;

public class FakeTimeSource implements TimeSource {
    private long currentTimeNano;
    private long incrementNanos;

    public FakeTimeSource() {
        currentTimeNano = 0;
        this.incrementNanos = 1L;
    }


    @Override
    public long currentTimeNano() {
        currentTimeNano += incrementNanos;
        return currentTimeNano;
    }

    @Override
    public void wakeUpAt(long timestamp, TimeUnit timeUnit) throws InterruptedException {
        currentTimeNano = timeUnit.toNanos(timestamp);
    }

    @Override
    public void wakeUpNow() {
        currentTimeNano += incrementNanos;
    }
}
