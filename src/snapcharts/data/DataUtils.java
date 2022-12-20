/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.data;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for Data.
 */
public class DataUtils {

    // Map of known formats
    private static Map<Integer,DecimalFormat>  _knownFormats = new HashMap<>();

    /**
     * Returns cell data for string.
     */
    public static String[][] getCellData(String aString)
    {
        String[] lines = aString.split("\\s*(\n|\r\n)\\s*");
        String[][] cells = new String[lines.length][];
        int lineCount = 0;

        for (String line : lines) {
            String[] fields = line.split("\\s*\t\\s*");
            cells[lineCount++] = fields;
        }

        if (lineCount != lines.length)
            cells = Arrays.copyOf(cells, lineCount);
        return cells;
    }

    /**
     * Guess data type.
     */
    public static DataType guessDataType(String[][] theCells)
    {
        int fieldCount = getFieldCount(theCells);

        if (fieldCount == 0)
            return DataType.UNKNOWN;
        if (fieldCount == 1)
            return DataType.IY;

        int numCount = 0;
        int strCount = 0;
        for (String[] line : theCells) {
            if (line.length == 0)
                continue;
            String str = line[0];

            try {
                Double.parseDouble(str);
                numCount++;
            } catch (Exception e) {
                strCount++;
            }
        }

        if (numCount == 0 && strCount == 0)
            return DataType.UNKNOWN;

        if (strCount == 0 || numCount > strCount * 4)
            return DataType.XY;
        return DataType.CY;
    }

    /**
     * Returns the number of fields in cells.
     */
    public static int getFieldCount(String[][] theCells)
    {
        int oneCount = 0;
        int twoCount = 0;

        for (String[] line : theCells) {
            if (line.length > 1)
                twoCount++;
            else if (line.length > 0)
                oneCount++;
        }

        if (twoCount > oneCount * 2)
            return 2;
        if (oneCount > 1)
            return 1;
        return 0;
    }

    public static String formatValue(double aValue)
    {
        DecimalFormat fmt = getFormatForValue(aValue);
        return fmt.format(aValue);
    }

    /**
     * Returns a formatter for significant digits.
     */
    public static DecimalFormat getFormatForValue(double aValue)
    {
        int sigDigits = getSigDigits(aValue);
        DecimalFormat fmt = getFormatForSigDigits(sigDigits);
        return fmt;
    }

    /**
     * Returns a formatter for significant digits.
     */
    public static DecimalFormat getFormatForSigDigits(int aCount)
    {
        DecimalFormat fmt = _knownFormats.get(aCount);
        if (fmt != null) return fmt;

        String format = "#.###";
        for (int i = 0; i < aCount; i++)
            format += '#';
        fmt = new DecimalFormat(format);
        _knownFormats.put(aCount, fmt);
        return fmt;
    }

    /**
     * Returns the number of significant digits for a number.
     */
    public static int getSigDigits(double aValue)
    {

        // 1 = 0
        // 5 = 1
        // .5 = 0
        // .01 = 0
        // .05 = -1
        //int count = (int) Math.ceil(Math.log10(aValue));

        int count = 0;
        double val = Math.abs(aValue);
        while (val < 1) {
            count++;
            val *= 10;
        }
        return count;
    }
}
