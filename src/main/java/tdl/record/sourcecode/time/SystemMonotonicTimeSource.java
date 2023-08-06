package tdl.record.sourcecode.time;

import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class SystemMonotonicTimeSource implements TimeSource {
    private static final Logger log = getLogger(SystemMonotonicTimeSource.class);
    private final long referenceTime;
    private final Object lockObj;

    public SystemMonotonicTimeSource() {
        referenceTime = System.nanoTime();
        lockObj = new Object();
    }

    @Override
    public long currentTimeNano() {
        return System.nanoTime() - referenceTime;
    }

    @Override
    public void wakeUpAt(long timestamp, TimeUnit timeUnit) throws InterruptedException {
        long currentTimestampNano = currentTimeNano();
        long targetTimestampNano = timeUnit.toNanos(timestamp);

        long timeToSleepMillis = TimeUnit.NANOSECONDS
                .toMillis(targetTimestampNano - currentTimestampNano);

        if (timeToSleepMillis > 1) {
            log.debug("Sleep for: {} millis", timeToSleepMillis);
            synchronized (lockObj) {
                lockObj.wait(timeToSleepMillis);
            }
        }
    }

    @Override
    public void wakeUpNow() {
        synchronized (lockObj) {
            lockObj.notifyAll();
        }
    }
}
