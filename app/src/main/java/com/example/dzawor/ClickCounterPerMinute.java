package com.example.dzawor;

import java.time.Duration;
import java.time.Instant;

public class ClickCounterPerMinute {
    private static final int MINUTE = 60;
    private final int maxClicks;
    private Instant time = Instant.now();
    private int clicks = 100;

    public ClickCounterPerMinute(int maxClicks) {
        this.maxClicks = maxClicks;
    }

    public boolean clickNextAndValidate() {
        Instant timeNow = Instant.now();
        int timeSeparation = MINUTE / maxClicks;
        if (Duration.between(time, timeNow).getSeconds() > timeSeparation) {
            time = timeNow;
            return true;
        }
        return false;
    }

}
