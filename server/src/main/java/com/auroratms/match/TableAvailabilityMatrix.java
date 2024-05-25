package com.auroratms.match;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Class for marking available tables and finding tables with available time
 * Matches can be scheduled from 8 am 9:30 pm i.e. ending at 10 pm.  Slots are 30 minutes in duration
 */
public class TableAvailabilityMatrix {

    private static final Logger log = LoggerFactory.getLogger(TableAvailabilityMatrix.class);
    private boolean[][] availableTableTimeSlotsMatrix;

    // first time slot is at 8 am, last at 10 pm
    public static final double FIRST_TIME_SLOT = 8.0d;
    public static final double LAST_TIME_SLOT = 22.0d;

    // time slots are half an hour long
    public static final double TIME_SLOT_SIZE = 0.5d;
    public static final int TIME_SLOT_SIZE_INT = 30;

    /**
     * @param totalAvailableTables
     */
    public TableAvailabilityMatrix(int totalAvailableTables) {
        // number of 30 minute time slots between 8 am and 10 pm
        int timeSlots = (int) ((LAST_TIME_SLOT - FIRST_TIME_SLOT) / TIME_SLOT_SIZE);
        availableTableTimeSlotsMatrix = new boolean[totalAvailableTables][timeSlots];
        for (boolean[] tableTimeSlots : availableTableTimeSlotsMatrix) {
            Arrays.fill(tableTimeSlots, true);
        }
    }

    /**
     * Marks table as unavailable for specified duration
     *
     * @param tableNum table number 1 based
     * @param duration in 30 minute increments
     */
    public void markTableAsUnavailable(int tableNum, double startTime, int duration) {
        int startTimeSlotIndex = timeSlotIndex(startTime);
        int timeSlotsNeeded = calculateTimeSlotsNeeded (duration);
        int endTimeSlotIndex = startTimeSlotIndex + timeSlotsNeeded;
        int tableIndex = tableNum - 1;
        boolean[] tableTimeSlots = this.availableTableTimeSlotsMatrix[tableIndex];
        for (int i = startTimeSlotIndex; i < endTimeSlotIndex; i++) {
            // mark it as unavailable
            tableTimeSlots[i] = false;
        }
    }

    private int calculateTimeSlotsNeeded(int duration) {
        return (int) Math.ceil((double)duration / (double)TIME_SLOT_SIZE_INT);
    }

    /**
     * Finds available table time on any table
     *
     * @param startTime
     * @param duration
     * @return
     */
    public AvailableTableInfo findAvailableTable(double startTime, int duration) {
        return this.findAvailableTable(startTime, duration, 1, false);
    }

    /**
     * Finds available table time starting at given start time with specified duration and starting with table number
     *
     * @param startTime           start looking at this start time
     * @param duration            required duration
     * @param startingTableNumber start searching with this table number
     * @param mustStartAtStartTime if true start time found must match desired start time
     * @return
     */
    public AvailableTableInfo findAvailableTable(double startTime, int duration, int startingTableNumber, boolean mustStartAtStartTime) {
        AvailableTableInfo availableTableInfo = null;
        try {
            log.info ("find table at startTime = " + startTime + " for duration: " + duration + " starting with table #: " + startingTableNumber + " mustStartAtStartTime " + mustStartAtStartTime);
            int timeSlotsNeeded = calculateTimeSlotsNeeded(duration);
            for (int tableIndex = (startingTableNumber - 1); tableIndex < this.availableTableTimeSlotsMatrix.length; tableIndex++) {
                boolean[] tableTimeSlots = this.availableTableTimeSlotsMatrix[tableIndex];
                double foundStartTime = findEarliestAvailableTable(startTime, timeSlotsNeeded, tableTimeSlots);
                // if found desired time on this table
                if (foundStartTime != 0) {
                    if ((mustStartAtStartTime && foundStartTime == startTime) ||
                        (!mustStartAtStartTime && foundStartTime >= startTime)) {
                        if (availableTableInfo == null) {
                            log.info("initial foundStartTime = " + foundStartTime);
                            availableTableInfo = new AvailableTableInfo();
                            availableTableInfo.tableNum = tableIndex + 1;
                            availableTableInfo.startTime = foundStartTime;
                            availableTableInfo.duration = duration;
                        } else {
                            // if time found is earlier lets use it instead of the later time to compress schedule
                            if (foundStartTime < availableTableInfo.startTime) {
                                log.info("earlier foundStartTime = " + foundStartTime);
                                availableTableInfo.tableNum = tableIndex + 1;
                                availableTableInfo.startTime = foundStartTime;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return availableTableInfo;
    }

    /**
     * @param startTime
     * @param timeSlotsNeeded
     * @param tableTimeSlots
     * @return
     */
    private double findEarliestAvailableTable(double startTime, int timeSlotsNeeded, boolean[] tableTimeSlots) {
        double foundStartTime = 0d;
        boolean bContinueSearching = true;
        do {
            boolean allAvailable = true;
            int startTimeSlot = timeSlotIndex(startTime);
            if (startTimeSlot + timeSlotsNeeded <= tableTimeSlots.length) {
                int endTimeSlotIndex = startTimeSlot + timeSlotsNeeded;
                for (int timeSlotIndex = startTimeSlot; timeSlotIndex < endTimeSlotIndex; timeSlotIndex++) {
                    if (!tableTimeSlots[timeSlotIndex]) {
                        allAvailable = false;
                        break;
                    }
                }
            } else {
                allAvailable = false;
            }
            if (allAvailable) {
                foundStartTime = startTime;
                bContinueSearching = false;
            } else {
                if (startTimeSlot + timeSlotsNeeded < tableTimeSlots.length) {
                    startTime += TIME_SLOT_SIZE;
                } else {
                    bContinueSearching = false;
                }
            }
        } while (bContinueSearching);
        return foundStartTime;
    }

    private int timeSlotIndex(double startTime) {
        double numTimeSlots = ((startTime - FIRST_TIME_SLOT) / TIME_SLOT_SIZE);
        return (int) (numTimeSlots);
    }

    public static class AvailableTableInfo {
        int tableNum;
        double startTime;
        int duration;
    }
}
