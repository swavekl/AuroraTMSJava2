package com.auroratms.match;

import java.util.Arrays;

/**
 * Class for marking available tables and finding tables with available time
 * Matches can be scheduled from 8 am 9:30 pm i.e. ending at 10 pm.  Slots are 30 minutes in duration
 */
public class TableAvailabilityMatrix {

    private boolean[][] availableTableTimeSlotsMatrix;

    // first time slot is at 8 am, last at 10 pm
    private static final double FIRST_TIME_SLOT = 8.0d;
    private static final double LAST_TIME_SLOT = 22.0d;

    // time slots are half an hour long
    private static final double TIME_SLOT_SIZE = 0.5d;
    private static final double TIME_SLOT_SIZE_INT = 30;

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
        int timeSlotsNeeded = (int) (duration / TIME_SLOT_SIZE_INT);
        int endTimeSlotIndex = startTimeSlotIndex + timeSlotsNeeded;
        int tableIndex = tableNum - 1;
        boolean[] tableTimeSlots = this.availableTableTimeSlotsMatrix[tableIndex];
        for (int i = startTimeSlotIndex; i < endTimeSlotIndex; i++) {
            // mark it as unavailable
            tableTimeSlots[i] = false;
        }
    }

    /**
     * Finds available table time on any table
     *
     * @param startTime
     * @param duration
     * @return
     */
    public AvailableTableInfo findAvailableTable(double startTime, int duration) {
        return this.findAvailableTable(startTime, duration, 1);
    }

    /**
     * Finds available table time starting at given start time with specified duration and starting with table number
     *
     * @param startTime           start looking at this start time
     * @param duration            required duration
     * @param startingTableNumber start searching with this table number
     * @return
     */
    public AvailableTableInfo findAvailableTable(double startTime, int duration, int startingTableNumber) {
        AvailableTableInfo availableTableInfo = null;
        int timeSlotsNeeded = (int) (duration / TIME_SLOT_SIZE_INT);
        for (int tableIndex = (startingTableNumber - 1); tableIndex < this.availableTableTimeSlotsMatrix.length; tableIndex++) {
            boolean[] tableTimeSlots = this.availableTableTimeSlotsMatrix[tableIndex];
            double foundStartTime = findEarliestAvailableTable(startTime, timeSlotsNeeded, tableTimeSlots);
            if (foundStartTime != 0) {
                if (availableTableInfo == null) {
                    availableTableInfo = new AvailableTableInfo();
                    availableTableInfo.tableNum = tableIndex + 1;
                    availableTableInfo.startTime = foundStartTime;
                    availableTableInfo.duration = duration;
                } else {
                    // if time found is earlier lets use it instead of the later time to compress schedule
                    if (foundStartTime < availableTableInfo.startTime) {
                        availableTableInfo.tableNum = tableIndex + 1;
                        availableTableInfo.startTime = foundStartTime;
                    }
                }
            }
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
            for (int timeSlotIndex = startTimeSlot; timeSlotIndex < (startTimeSlot + timeSlotsNeeded); timeSlotIndex++) {
                if (!tableTimeSlots[timeSlotIndex]) {
                    allAvailable = false;
                    break;
                }
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
