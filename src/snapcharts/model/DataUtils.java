package snapcharts.model;
import java.util.Arrays;

/**
 * Utilities for Data.
 */
public class DataUtils {

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
        String valStrs[] = aStr.split("\\s*,\\s*");
        int len = valStrs.length;
        int count = 0;
        double vals[] = new double[len];
        for (String valStr : valStrs) {
            if (valStr.length() > 0) {
                try {
                    double val = Double.valueOf(valStr);
                    vals[count++] = val;
                } catch (Exception e) {
                }
            }
        }
        if (count<len)
            vals = Arrays.copyOf(vals, count);
        return vals;
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
}
