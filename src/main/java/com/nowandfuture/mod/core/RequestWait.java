package com.nowandfuture.mod.core;

public class RequestWait {

    private long lastRequestTime = 0;
    private static final long DEFAULT_REQUEST_WAIT_UNIT = 250; // ms
    private static final long DEFAULT_BASE_WAIT_TIME = 500; // ms

    private final long baseTimeWait;
    private final long timeWaitUnit;
    private int idx = 0;
    private final long[] waitTimes;

    private RequestWait(){
        this(DEFAULT_BASE_WAIT_TIME, DEFAULT_REQUEST_WAIT_UNIT, new long[]{1, 2, 4, 8, 12, 16, 20, 24, 28, 32});
    }

    private RequestWait(long baseTimeWait, long defaultWaitTimeUnit, long[] waitTimes){
        if(waitTimes.length <= 0){
            throw new RuntimeException("The wait times should not be empty!");
        }
        this.timeWaitUnit = defaultWaitTimeUnit;
        this.waitTimes = waitTimes;
        this.baseTimeWait = baseTimeWait;
    }

    public static RequestWait create(long baseTimeWait, long defaultWaitTime, long[] waitTimes){
        return new RequestWait(baseTimeWait, defaultWaitTime, waitTimes);
    }

    public static RequestWait DEFAULT(){
        return new RequestWait();
    }

    public boolean tryAccept(){
        long lastTime = this.lastRequestTime;
        long curTime = System.currentTimeMillis();

        if(curTime - lastTime > getCurWaitTime()){
            this.lastRequestTime = curTime;
            return true;
        }

        return false;
    }

    public long request(boolean success){
        if(!success && idx < waitTimes.length - 1){
            idx ++;
        }
        if(success){
            this.idx = 0;
        }

        return getCurWaitTime();
    }

    public long getCurWaitTime(){
        return waitTimes[idx] * timeWaitUnit;
    }

    public long leftWaitTime(){
        long left = getCurWaitTime() - (System.currentTimeMillis() - lastRequestTime);
        return left > 0 ? left: 0;
    }
}
