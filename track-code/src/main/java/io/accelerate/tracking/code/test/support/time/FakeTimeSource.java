package io.accelerate.tracking.code.test.support.time;

import io.accelerate.tracking.code.time.TimeSource;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class FakeTimeSource implements TimeSource {
    private long currentTimeNano;
    private long incrementNanos;
    private final ArrayList<WakeUpListener> wakeUpListeners;

    public FakeTimeSource() {
        currentTimeNano = 0;
        this.incrementNanos = 1L;
        this.wakeUpListeners = new ArrayList<>();
    }


    @Override
    public long currentTimeNano() {
        currentTimeNano += incrementNanos;
        return currentTimeNano;
    }

    @Override
    public void wakeUpAt(long timestamp, TimeUnit timeUnit) {
        currentTimeNano = timeUnit.toNanos(timestamp);
        notifyWakeUpAtListeners();
    }

    @Override
    public void wakeUpNow() {
        currentTimeNano += incrementNanos;
    }


    public void addWakeUpAtListener(WakeUpListener wakeUpListener) {
        wakeUpListeners.add(wakeUpListener);
    }

    private void notifyWakeUpAtListeners() {
        wakeUpListeners.forEach(WakeUpListener::wakeUpInvoked);
    }
}
