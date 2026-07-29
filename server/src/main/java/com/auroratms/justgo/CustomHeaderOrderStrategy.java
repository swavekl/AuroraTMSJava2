package com.auroratms.justgo;

import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import java.util.Comparator;
import java.util.List;

public class CustomHeaderOrderStrategy<T> extends HeaderColumnNameMappingStrategy<T> {

    public CustomHeaderOrderStrategy(Class<T> type, List<String> headerOrder) {
        setType(type);

        // OpenCSV normalizes column names internally to uppercase.
        // We compare against uppercase versions of your defined header list.
        List<String> upperHeaderOrder = headerOrder.stream()
                .map(String::toUpperCase)
                .toList();

        setColumnOrderOnWrite(Comparator.comparingInt(col -> {
            int index = upperHeaderOrder.indexOf(col.toUpperCase());
            return index != -1 ? index : Integer.MAX_VALUE;
        }));
    }
}
