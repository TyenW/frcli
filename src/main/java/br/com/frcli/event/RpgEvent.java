package br.com.frcli.event;

public abstract class RpgEvent {
    private final long timestamp;

    protected RpgEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }
}
