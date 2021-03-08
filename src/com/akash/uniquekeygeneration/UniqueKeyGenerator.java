package com.akash.uniquekeygeneration;

import java.time.Instant;

/**
 * Create a singleton object to have synchronization among the same machine
 */
public class UniqueKeyGenerator {

    /*
     * unique id will
     * consist of random bits 12 bits + -> no. of sequence 2^12
     * machine id of 4 bits + -> no. of machines 2^4=4096
     * time based id of 41 bits -> no. of time based ids = 2^41
     */

    public static final int MACHINE_ID_BITS = 4;
    public static final int SEQUENCE_ID_BITS = 12;

    private final int machineId;
    private volatile long sequenceId = 0L;
    private volatile long lastAccessTime = -1;

    private static final long START_OF_EPOCH_TIME = Instant.parse("2000-01-01T00:00:00Z").toEpochMilli();
    private static final int MAX_MACHINE_ID = (int) Math.pow(2, 4) - 1;
    private static final int MAX_SEQUENCE_ID = (int) Math.pow(2, 12) - 1;

    public UniqueKeyGenerator(final int machineId) {
        if (machineId > MAX_MACHINE_ID)
            throw new IllegalArgumentException();
        this.machineId = machineId;
    }

    public synchronized long getNextSequenceId() {
        long currentTime = getCurrentTime();
        if (currentTime == lastAccessTime) {
            sequenceId = (sequenceId + 1) % MAX_SEQUENCE_ID;
            if(sequenceId == 0){
                currentTime = getNextIntervalTime(currentTime);
            }
        } else {
            sequenceId = 0;
        }
        lastAccessTime = currentTime;
        long id = currentTime << (MACHINE_ID_BITS + SEQUENCE_ID_BITS);
        id = id | (machineId << SEQUENCE_ID_BITS);
        id = id | sequenceId;
        return id;
    }

    private long getNextIntervalTime(long currentTime) {
        while(currentTime == lastAccessTime){
            currentTime = getCurrentTime();
        }
        return currentTime;
    }

    private long getCurrentTime() {
        return Instant.now().toEpochMilli() - START_OF_EPOCH_TIME;
    }

}
