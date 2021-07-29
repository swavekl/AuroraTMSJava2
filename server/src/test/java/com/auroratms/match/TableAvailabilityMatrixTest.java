package com.auroratms.match;

import org.junit.Test;

import static org.junit.Assert.*;

public class TableAvailabilityMatrixTest {

    @Test
    public void testMarkingOnOneTable () {
        TableAvailabilityMatrix matrix = new TableAvailabilityMatrix(1);
        matrix.markTableAsUnavailable(1, 9.0d, 90);

        TableAvailabilityMatrix.AvailableTableInfo availableTable = matrix.findAvailableTable(9.0d, 60, 1);
        assertEquals ("table not found", 1, availableTable.tableNum);
        assertEquals ("wrong start time", 10.5d, availableTable.startTime, 0.0d);

    }

    @Test
    public void testMarkingOnOneTableLate () {
        TableAvailabilityMatrix matrix = new TableAvailabilityMatrix(1);
        matrix.markTableAsUnavailable(1, 9.0d, 90);
        matrix.markTableAsUnavailable(1, 10.5d, 180);
        matrix.markTableAsUnavailable(1, 14.0d, 120);
        matrix.markTableAsUnavailable(1, 16.5d, 180);
        matrix.markTableAsUnavailable(1, 20.0d, 60);

        TableAvailabilityMatrix.AvailableTableInfo availableTable = matrix.findAvailableTable(9.0d, 60);
        assertEquals ("table not found", 1, availableTable.tableNum);
        assertEquals ("wrong start time", 21.0d, availableTable.startTime, 0.0d);

        matrix.markTableAsUnavailable(1, availableTable.startTime, availableTable.duration);
        TableAvailabilityMatrix.AvailableTableInfo availableTable2 = matrix.findAvailableTable(9.0d, 60);
        assertNull("wrong table", availableTable2);
    }

    @Test
    public void testMarkingOnManyTables () {
        TableAvailabilityMatrix matrix = new TableAvailabilityMatrix(2);
        matrix.markTableAsUnavailable(1, 9.0d, 90);
        matrix.markTableAsUnavailable(2, 9.0d, 90);

        // find table and mark it as unavailable
        TableAvailabilityMatrix.AvailableTableInfo availableTable = matrix.findAvailableTable(10.5d, 30);
        assertEquals ("table not found", 1, availableTable.tableNum);
        assertEquals ("wrong start time", 10.5d, availableTable.startTime, 0.0d);
        matrix.markTableAsUnavailable(availableTable.tableNum, availableTable.startTime, availableTable.duration);

        // find another table at the same time
        TableAvailabilityMatrix.AvailableTableInfo availableTable2 = matrix.findAvailableTable(10.5d, 30);
        assertEquals ("table not found", 2, availableTable2.tableNum);
        assertEquals ("wrong start time", 10.5d, availableTable2.startTime, 0.0d);

    }
}
