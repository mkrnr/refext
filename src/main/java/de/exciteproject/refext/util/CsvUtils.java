package de.exciteproject.refext.util;

import java.util.ArrayList;
import java.util.List;

public class CsvUtils {

    public static String normalize(String inputString) {
        return inputString.replaceAll("\t", "").replaceAll("\n", "").trim();
    }

    public static List<String> readColumn(int columnIndex, String inputString, String columnSeparator) {
        List<String> columnValues = new ArrayList<String>();
        for (String line : inputString.split(System.getProperty("line.separator"))) {
            String[] columns = line.split(columnSeparator);
            if (columnIndex < columns.length) {
                columnValues.add(columns[columnIndex]);
            }
        }
        return columnValues;

    }
}
