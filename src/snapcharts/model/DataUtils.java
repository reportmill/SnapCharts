package snapcharts.model;
import snap.util.ListSel;
import snap.util.SnapUtils;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for Data.
 */
public class DataUtils {

    // A formatter to format double without exponent
    private static DecimalFormat _doubleFmt = new DecimalFormat("0.#########");

    // Map of known formats
    private static Map<Integer,DecimalFormat>  _knownFormats = new HashMap<>();

    /**
     * Adds data points to given DataSet for given data arrays.
     */
    public static void addDataSetPoints(DataSet aDataSet, double[] dataX, double[] dataY, double[] dataZ, String[] dataC)
    {
        // Get min length of staged data
        int xlen = dataX!=null ? dataX.length : Integer.MAX_VALUE;
        int ylen = dataY!=null ? dataY.length : Integer.MAX_VALUE;
        int zlen = dataZ!=null ? dataZ.length : Integer.MAX_VALUE;
        int clen = dataC!=null ? dataC.length : Integer.MAX_VALUE;
        int len = Math.min(xlen, Math.min(ylen, Math.min(zlen, clen)));

        // Iterate over data arrays and add to DataSet
        for (int i=0; i<len; i++) {
            Double valX = dataX!=null ? dataX[i] : null;
            Double valY = dataY!=null ? dataY[i] : null;
            Double valZ = dataZ!=null ? dataZ[i] : null;
            String valC = dataC!=null ? dataC[i] : null;
            aDataSet.addPointXYZC(valX, valY, valZ, valC);
        }
    }

    /**
     * Deletes data for given DataSet and selection.
     */
    public static void deleteDataSetDataForSelection(DataSet aDataSet, ListSel aSel)
    {
        int indexes[] = aSel.getIndexes();
        for (int i = indexes.length - 1; i >= 0; i--) {
            int ind = indexes[i];
            if (ind < aDataSet.getPointCount())
                aDataSet.removePoint(ind);
        }
    }

    /**
     * Replaces data for given DataSet and selection.
     */
    public static void replaceDataSetDataForSelection(DataSet aDatatSet, ListSel aSel, String theCells[][])
    {
        DataType dataType = aDatatSet.getDataType();
        int indexes[] = aSel.getIndexes();

        // Remove currently selected cells
        for (int i=indexes.length-1; i>=0; i--) {
            int ind = indexes[i];
            if (ind<aDatatSet.getPointCount())
                aDatatSet.removePoint(ind);
        }

        // Update DataType
        if (dataType==DataType.UNKNOWN || aDatatSet.getPointCount()==0) {
            dataType = DataUtils.guessDataType(theCells);
        }

        // Add Cells
        for (String line[] : theCells) {

            if (line.length==0) continue;

            // Get vals: If only one val on line it's Y, X is index
            String valX = line.length>1 ? line[0] : null;
            String valY = line.length>1 ? line[1] : line[0];

            switch (dataType) {

                case IY: {
                    double y = valY != null ? SnapUtils.doubleValue(valY) : 0;
                    aDatatSet.addPointXYZC(null, y, null, null);
                    break;
                }

                case XY: {
                    double x = valX != null ? SnapUtils.doubleValue(valX) : 0;
                    double y = valY != null ? SnapUtils.doubleValue(valY) : 0;
                    aDatatSet.addPointXYZC(x, y, null, null);
                    break;
                }

                case CY: {
                    double y = valY != null ? SnapUtils.doubleValue(valY) : 0;
                    aDatatSet.addPointXYZC(null, y, null, valX);
                    break;
                }

                default:
                    System.out.println("DataSet.replaceData: Unsupported data type: " + dataType);
                    return;
            }
        }
    }

    /**
     * Returns cell data for string.
     */
    public static String[][] getCellData(String aString)
    {
        String lines[] = aString.split("\\s*(\n|\r\n)\\s*");
        String cells[][] = new String[lines.length][];
        int lineCount = 0;

        for (String line : lines) {
            String fields[] = line.split("\\s*\t\\s*");
            cells[lineCount++] = fields;
        }

        if (lineCount!=lines.length)
            cells = Arrays.copyOf(cells, lineCount);
        return cells;
    }

    /**
     * Return string for double array.
     */
    public static String getStringForDoubleArray(double theValues[])
    {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i=0, iMax=theValues.length-1; ; i++) {
            String str = _doubleFmt.format(theValues[i]);
            sb.append(str);
            if (i == iMax)
                return sb.append(']').toString();
            sb.append(", ");
        }
    }

    /** Returns a String for an array of data values. */
    //public static String getStringForDoubleArray(double theVals[]) {
    //    StringBuffer sb = new StringBuffer();
    //    for (double val : theVals) sb.append(val).append(", ");
    //    sb.delete(sb.length()-2, sb.length()); return sb.toString(); }

    /**
     * Returns an array of double values for given comma separated string.
     */
    public static double[] getDoubleArrayForString(String aStr)
    {
        // Get string, stripped of surrounding non-number chars
        String str = aStr.trim();
        int start = 0; while (start<str.length() && !isNumChar(str, start)) start++;
        int end = str.length(); while(end>0 && !isNumChar(str, end-1)) end--;
        str = str.substring(start, end);

        // Get strings for values separated by comma
        String valStrs[] = str.split("\\s*,\\s*");
        int len = valStrs.length;

        // Create array for return vals
        double vals[] = new double[len];
        int count = 0;

        // Iterate over strings and add valid numbers
        for (String valStr : valStrs) {
            if (valStr.length() > 0) {
                try {
                    double val = Double.valueOf(valStr);
                    vals[count++] = val;
                }
                catch (Exception e)  { }
            }
        }

        // Return vals (trimmed to size)
        return count<len ? Arrays.copyOf(vals, count) : vals;
    }

    /**
     * Returns an array of String values for given comma separated string.
     */
    public static String[] getStringArrayForString(String aStr)
    {
        String valStrs[] = aStr.split("\\s*,\\s*");
        int len = valStrs.length;
        int count = 0;
        String vals[] = new String[len];
        for (String valStr : valStrs) {
            if (valStr.startsWith("\""))
                valStr = valStr.substring(1);
            if (valStr.endsWith("\""))
                valStr = valStr.substring(0, valStr.length()-1);
            vals[count++] = valStr;
        }
        return vals;
    }

    /**
     * Guess data type.
     */
    public static DataType guessDataType(String theCells[][])
    {
        int fieldCount = getFieldCount(theCells);

        if (fieldCount==0)
            return DataType.UNKNOWN;
        if (fieldCount==1)
            return DataType.IY;

        int numCount = 0;
        int strCount = 0;
        for (int i=0; i<theCells.length; i++) {
            String line[] = theCells[i]; if (line.length==0) continue;
            String str = line[0];

            try {
                Double.parseDouble(str);
                numCount++;
            }
            catch (Exception e) {
                strCount++;
            }
        }

        if (numCount==0 && strCount==0)
            return DataType.UNKNOWN;

        if (strCount==0 || numCount>strCount*4)
            return DataType.XY;
        return DataType.CY;
    }

    /**
     * Returns the number of fields in cells.
     */
    public static int getFieldCount(String theCells[][])
    {
        int oneCount = 0;
        int twoCount = 0;

        for (int i=0; i<theCells.length; i++) {
            String line[] = theCells[i];
            if (line.length>1)
                twoCount++;
            else if (line.length>0)
                oneCount++;
        }

        if (twoCount>oneCount*2)
            return 2;
        if (oneCount>1)
            return 1;
        return 0;
    }

    /**
     * Returns whether char at given index in given string is number char.
     */
    private static boolean isNumChar(String aStr, int anIndex)
    {
        char c = aStr.charAt(anIndex);
        return Character.isDigit(c) || c=='.' || c=='-';
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
        if (fmt!=null) return fmt;

        String format = "#.###";
        for (int i=0; i<aCount; i++) format += '#';
        fmt = new DecimalFormat(format);
        _knownFormats.put(aCount, fmt);
        return fmt;
    }

    /**
     * Returns the number of significan digits for a number.
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
        while (val<1) { count++; val*= 10; }
        return count;
    }
}
